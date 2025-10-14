// Simple React without build step using CDN-free approach via Tiny React-like implementation
// For production you'd typically bundle with Vite/React, but here we keep it inline and portable.

(async function(){
  const root = document.getElementById('menu-react-root');
  const api = '/api';

  function h(tag, props, ...children){
    const el = document.createElement(tag);
    for(const k in (props||{})){
      if(k === 'className') el.setAttribute('class', props[k]);
      else if(k.startsWith('on') && typeof props[k] === 'function') el.addEventListener(k.substring(2).toLowerCase(), props[k]);
      else el.setAttribute(k, props[k]);
    }
    for(const c of children.flat()){
      if (c == null) continue;
      if (typeof c === 'string') el.appendChild(document.createTextNode(c));
      else el.appendChild(c);
    }
    return el;
  }

  function Spinner(){
    return h('div', { className:'text-center text-gray-500 py-8' }, 'Loading...');
  }

  function MenuCard(item, onReserve){
    return h('div', { className:'bg-white rounded-lg shadow p-4 flex flex-col' },
      h('div', { className:'flex-1' },
        h('h3', { className:'text-lg font-semibold mb-1' }, item.name||''),
        h('p',  { className:'text-gray-600 text-sm mb-2' }, item.description||''),
        (item.dishesIncluded ? h('div', { className:'text-xs text-gray-500 mb-2' }, `Included dishes: ${item.dishesIncluded}`) : null),
        h('div',{ className:'text-sm text-gray-700 mb-1' }, `Category: ${item.category||''}`),
        h('div',{ className:'text-sm text-gray-700 mb-1' }, `Cuisine: ${item.cuisine||''}`),
        h('div',{ className:'text-sm text-gray-700 mb-1' }, `Event: ${item.eventType||''}`),
        h('div',{ className:'font-bold text-gray-900' }, `Rs ${Number(item.price||0).toFixed(2)}`)
      ),
      h('div', { className:'pt-3' },
        h('button', { className:'bg-[#e4a801] hover:bg-[#d27c00] text-white px-4 py-2 rounded w-full', onClick: ()=> onReserve(item) }, 'Reserve')
      )
    );
  }

  // State
  let menuItems = null;
  let selectedItem = null;

  async function fetchMenu(){
    const res = await fetch(`${api}/menu`);
    if(!res.ok) throw new Error('Failed to load menu');
    return res.json();
  }

  function render(){
    root.innerHTML='';
    if(!menuItems){
      root.appendChild(Spinner());
      return;
    }
    const header = h('div', { className:'flex items-center justify-between mb-4' },
      h('h2', { className:'text-2xl font-bold' }, 'Menu'),
      h('div', { className:'text-sm text-gray-600' }, 'Select items to reserve your event')
    );

    const grid = h('div', { className:'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4' },
      menuItems.map(m => MenuCard(m, openReserve))
    );

    const reserveModal = buildReserveModal();

    root.appendChild(header);
    root.appendChild(grid);
    root.appendChild(reserveModal);
  }

  function buildReserveModal(){
    const modal = h('div', { id:'reserveModal', className:'hidden fixed inset-0 bg-black/40 flex items-center justify-center p-4' });
    const card = h('div', { className:'bg-white rounded-lg shadow-lg max-w-lg w-full p-5' },
      h('div', { className:'flex items-center justify-between mb-3' },
        h('h3', { className:'text-lg font-semibold' }, 'Reservation'),
        h('button', { className:'text-gray-600', onClick: closeReserve }, '✕')
      ),
      h('div', { className:'mb-3 text-sm text-gray-700' }, selectedItem ? `Menu: ${selectedItem.name} · Rs ${Number(selectedItem.price||0).toFixed(2)} per guest` : ''),
      (selectedItem && selectedItem.description ? h('div', { className:'mb-2 text-sm text-gray-600' }, selectedItem.description) : null),
      (selectedItem && selectedItem.dishesIncluded ? h('div', { className:'mb-3 text-xs text-gray-500' }, `Included dishes: ${selectedItem.dishesIncluded}`) : null),
      h('form', { id:'reserveForm', onSubmit: handleReserveSubmit },
        h('div', { className:'grid grid-cols-1 sm:grid-cols-2 gap-3' },
          h('div', {},
            h('label', { className:'text-sm text-gray-600' }, 'Event Date'),
            h('input', { id:'bkDate', type:'date', required:'true', className:'border rounded w-full px-3 py-2' })
          ),
          h('div', {},
            h('label', { className:'text-sm text-gray-600' }, 'Guests'),
            h('input', { id:'bkGuests', type:'number', min:'1', value:'50', className:'border rounded w-full px-3 py-2' })
          ),
          h('div', { className:'sm:col-span-2' },
            h('label', { className:'text-sm text-gray-600' }, 'Location'),
            h('input', { id:'bkLocation', className:'border rounded w-full px-3 py-2', placeholder:'Event address' })
          ),
          h('div', {},
            h('label', { className:'text-sm text-gray-600' }, 'Event Type'),
            h('input', { id:'bkEventType', className:'border rounded w-full px-3 py-2', placeholder:'Wedding, Corporate, ...' })
          ),
          h('div', {},
            h('label', { className:'text-sm text-gray-600' }, 'Dietary Requirements'),
            h('input', { id:'bkDietary', className:'border rounded w-full px-3 py-2', placeholder:'Veg, Vegan, Halal, Allergies' })
          )
        ),
        h('div', { className:'mt-4 flex items-center justify-end gap-2' },
          h('button', { type:'button', className:'px-4 py-2 rounded bg-gray-200', onClick: closeReserve }, 'Cancel'),
          h('button', { type:'submit', className:'px-4 py-2 rounded bg-[#e4a801] hover:bg-[#d27c00] text-white' }, 'Continue to Payment')
        )
      )
    );
    modal.appendChild(card);
    return modal;
  }

  function openReserve(item){
    selectedItem = item;
    const modal = document.getElementById('reserveModal');
    if(modal) modal.classList.remove('hidden');
  }

  function closeReserve(){
    const modal = document.getElementById('reserveModal');
    if(modal) modal.classList.add('hidden');
  }

  async function handleReserveSubmit(e){
    e.preventDefault();
    if(!selectedItem) return;
    // Create booking -> get invoice
    const body = {
      menuIds: [selectedItem.id],
      eventDate: document.getElementById('bkDate').value,
      guestCount: parseInt(document.getElementById('bkGuests').value||'0',10),
      location: document.getElementById('bkLocation').value,
      eventType: document.getElementById('bkEventType').value,
      dietaryRequirements: document.getElementById('bkDietary').value,
      addonIds: []
    };
    let invoice;
    try{
      const res = await fetch(`${api}/booking`, { method:'POST', headers:{ 'Content-Type':'application/json' }, credentials:'include', body: JSON.stringify(body) });
      if(res.status === 401){ window.location.href = '/login'; return; }
      if(!res.ok){ alert('Booking failed'); return; }
      const data = await res.json();
      invoice = { id: data.invoiceId, amount: data.amount };
    } catch(err){ alert('Booking failed'); return; }

    // Create Stripe Checkout Session
    try{
      const res = await fetch('/api/stripe/create-checkout-session', { method:'POST', headers:{ 'Content-Type':'application/json' }, body: JSON.stringify({ invoiceId: invoice.id, amount: invoice.amount }) });
      if(!res.ok){ alert('Payment init failed'); return; }
      const data = await res.json();
      const stripe = Stripe(data.publicKey);
      await stripe.redirectToCheckout({ sessionId: data.sessionId });
    } catch(err){ alert('Payment initialization failed'); }
  }

  try{
    menuItems = await fetchMenu();
  } catch(e){
    root.appendChild(h('div', { className:'text-center text-red-600 py-8' }, 'Failed to load menu'));
    return;
  }
  render();
})();


