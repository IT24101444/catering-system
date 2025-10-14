import React, { useEffect, useMemo, useRef, useCallback } from 'https://esm.sh/react@18.2.0';
import { createRoot } from 'https://esm.sh/react-dom@18.2.0/client';
import { useGesture } from 'https://esm.sh/@use-gesture/react@10.2.27';

const DEFAULT_IMAGES = [
  { src: 'https://images.unsplash.com/photo-1555244162-803834f70033?q=80&w=1200&auto=format&fit=crop', alt: 'Buffet catering table' },
  { src: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop', alt: 'Catering setup in venue' },
  { src: 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', alt: 'Canapés and finger food' },
  { src: 'https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1200&auto=format&fit=crop', alt: 'Wedding catering dessert table' },
  { src: 'https://images.unsplash.com/photo-1526318472351-c75fcf070305?q=80&w=1200&auto=format&fit=crop', alt: 'Grilled skewers platter' },
  { src: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1200&auto=format&fit=crop', alt: 'Salad and fresh dishes' },
  { src: 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?q=80&w=1200&auto=format&fit=crop', alt: 'Banquet table settings' },
  { src: 'https://images.unsplash.com/photo-1530041688517-3c6502ee4b4b?q=80&w=1200&auto=format&fit=crop', alt: 'Appetizer spread' },
  { src: 'https://images.unsplash.com/photo-1528716321680-815a8cdb8cbe?q=80&w=1200&auto=format&fit=crop', alt: 'Chef plating catering dish' },
  { src: 'https://images.unsplash.com/photo-1528756514091-dee5ecaa3278?q=80&w=1200&auto=format&fit=crop', alt: 'Dessert pastries' }
];

const clamp = (v, min, max) => Math.min(Math.max(v, min), max);
const normalizeAngle = d => ((d % 360) + 360) % 360;
const wrapAngleSigned = deg => { const a = (((deg + 180) % 360) + 360) % 360; return a - 180; };
const getDataNumber = (el, name, fallback) => { const attr = el.dataset[name] ?? el.getAttribute(`data-${name}`); const n = attr == null ? NaN : parseFloat(attr); return Number.isFinite(n) ? n : fallback; };

function buildItems(pool, seg) {
  const xCols = Array.from({ length: seg }, (_, i) => -37 + i * 2);
  const evenYs = [-4, -2, 0, 2, 4];
  const oddYs = [-3, -1, 1, 3, 5];
  const coords = xCols.flatMap((x, c) => { const ys = c % 2 === 0 ? evenYs : oddYs; return ys.map(y => ({ x, y, sizeX: 2, sizeY: 2 })); });
  const totalSlots = coords.length;
  const normalizedImages = (pool && pool.length ? pool : DEFAULT_IMAGES).map(image => typeof image === 'string' ? ({ src: image, alt: '' }) : ({ src: image.src || '', alt: image.alt || '' }));
  const usedImages = Array.from({ length: totalSlots }, (_, i) => normalizedImages[i % normalizedImages.length]);
  for (let i = 1; i < usedImages.length; i++) {
    if (usedImages[i].src === usedImages[i - 1].src) {
      for (let j = i + 1; j < usedImages.length; j++) {
        if (usedImages[j].src !== usedImages[i].src) { const tmp = usedImages[i]; usedImages[i] = usedImages[j]; usedImages[j] = tmp; break; }
      }
    }
  }
  return coords.map((c, i) => ({ ...c, src: usedImages[i].src, alt: usedImages[i].alt }));
}

function computeItemBaseRotation(offsetX, offsetY, sizeX, sizeY, segments) {
  const unit = 360 / segments / 2; const rotateY = unit * (offsetX + (sizeX - 1) / 2); const rotateX = unit * (offsetY - (sizeY - 1) / 2); return { rotateX, rotateY };
}

function DomeGallery({ images = DEFAULT_IMAGES, fit = 0.5, fitBasis = 'auto', minRadius = 600, maxRadius = Infinity, padFactor = 0.25, overlayBlurColor = '#060010', maxVerticalRotationDeg = 5, dragSensitivity = 20, enlargeTransitionMs = 300, segments = 35, dragDampening = 2, openedImageWidth = '250px', openedImageHeight = '350px', imageBorderRadius = '30px', openedImageBorderRadius = '30px', grayscale = true }) {
  const rootRef = useRef(null); const mainRef = useRef(null); const sphereRef = useRef(null); const frameRef = useRef(null); const viewerRef = useRef(null); const scrimRef = useRef(null); const focusedElRef = useRef(null); const originalTilePositionRef = useRef(null);
  const rotationRef = useRef({ x: 0, y: 0 }); const startRotRef = useRef({ x: 0, y: 0 }); const startPosRef = useRef(null); const draggingRef = useRef(false); const movedRef = useRef(false); const inertiaRAF = useRef(null); const openingRef = useRef(false); const openStartedAtRef = useRef(0); const lastDragEndAt = useRef(0);
  const scrollLockedRef = useRef(false);
  const lockScroll = useCallback(() => { if (scrollLockedRef.current) return; scrollLockedRef.current = true; document.body.classList.add('dg-scroll-lock'); }, []);
  const unlockScroll = useCallback(() => { if (!scrollLockedRef.current) return; if (rootRef.current?.getAttribute('data-enlarging') === 'true') return; scrollLockedRef.current = false; document.body.classList.remove('dg-scroll-lock'); }, []);
  const items = useMemo(() => buildItems(images, segments), [images, segments]);
  const applyTransform = (xDeg, yDeg) => { const el = sphereRef.current; if (el) { el.style.transform = `translateZ(calc(var(--radius) * -1)) rotateX(${xDeg}deg) rotateY(${yDeg}deg)`; } };
  const lockedRadiusRef = useRef(null);
  useEffect(() => {
    const root = rootRef.current; if (!root) return; const ro = new ResizeObserver(entries => { const cr = entries[0].contentRect; const w = Math.max(1, cr.width), h = Math.max(1, cr.height); const minDim = Math.min(w, h), maxDim = Math.max(w, h), aspect = w / h; let basis; switch (fitBasis) { case 'min': basis = minDim; break; case 'max': basis = maxDim; break; case 'width': basis = w; break; case 'height': basis = h; break; default: basis = aspect >= 1.3 ? w : minDim; } let radius = basis * fit; const heightGuard = h * 1.35; radius = Math.min(radius, heightGuard); radius = clamp(radius, minRadius, maxRadius); lockedRadiusRef.current = Math.round(radius); const viewerPad = Math.max(8, Math.round(minDim * padFactor)); root.style.setProperty('--radius', `${lockedRadiusRef.current}px`); root.style.setProperty('--viewer-pad', `${viewerPad}px`); root.style.setProperty('--overlay-blur-color', overlayBlurColor); root.style.setProperty('--tile-radius', imageBorderRadius); root.style.setProperty('--enlarge-radius', openedImageBorderRadius); root.style.setProperty('--image-filter', grayscale ? 'grayscale(1)' : 'none'); applyTransform(rotationRef.current.x, rotationRef.current.y); const enlargedOverlay = viewerRef.current?.querySelector('.enlarge'); if (enlargedOverlay && frameRef.current && mainRef.current) { const frameR = frameRef.current.getBoundingClientRect(); const mainR = mainRef.current.getBoundingClientRect(); const hasCustomSize = openedImageWidth && openedImageHeight; if (hasCustomSize) { const tempDiv = document.createElement('div'); tempDiv.style.cssText = `position: absolute; width: ${openedImageWidth}; height: ${openedImageHeight}; visibility: hidden;`; document.body.appendChild(tempDiv); const tempRect = tempDiv.getBoundingClientRect(); document.body.removeChild(tempDiv); const centeredLeft = frameR.left - mainR.left + (frameR.width - tempRect.width) / 2; const centeredTop = frameR.top - mainR.top + (frameR.height - tempRect.height) / 2; enlargedOverlay.style.left = `${centeredLeft}px`; enlargedOverlay.style.top = `${centeredTop}px`; } else { enlargedOverlay.style.left = `${frameR.left - mainR.left}px`; enlargedOverlay.style.top = `${frameR.top - mainR.top}px`; enlargedOverlay.style.width = `${frameR.width}px`; enlargedOverlay.style.height = `${frameR.height}px`; } } }); ro.observe(root); return () => ro.disconnect(); }, [fit, fitBasis, minRadius, maxRadius, padFactor, overlayBlurColor, grayscale, imageBorderRadius, openedImageBorderRadius, openedImageWidth, openedImageHeight]);
  useEffect(() => { applyTransform(rotationRef.current.x, rotationRef.current.y); }, []);
  const stopInertia = useCallback(() => { if (inertiaRAF.current) { cancelAnimationFrame(inertiaRAF.current); inertiaRAF.current = null; } }, []);
  const startInertia = useCallback((vx, vy) => { const MAX_V = 1.4; let vX = clamp(vx, -MAX_V, MAX_V) * 80; let vY = clamp(vy, -MAX_V, MAX_V) * 80; let frames = 0; const d = clamp(2 ?? 0.6, 0, 1); const frictionMul = 0.94 + 0.055 * d; const stopThreshold = 0.015 - 0.01 * d; const maxFrames = Math.round(90 + 270 * d); const step = () => { vX *= frictionMul; vY *= frictionMul; if (Math.abs(vX) < stopThreshold && Math.abs(vY) < stopThreshold) { inertiaRAF.current = null; return; } if (++frames > maxFrames) { inertiaRAF.current = null; return; } const nextX = clamp(rotationRef.current.x - vY / 200, -5, 5); const nextY = wrapAngleSigned(rotationRef.current.y + vX / 200); rotationRef.current = { x: nextX, y: nextY }; applyTransform(nextX, nextY); inertiaRAF.current = requestAnimationFrame(step); }; stopInertia(); inertiaRAF.current = requestAnimationFrame(step); }, [stopInertia]);
  useGesture({ onDragStart: ({ event }) => { if (focusedElRef.current) return; stopInertia(); const evt = event; draggingRef.current = true; movedRef.current = false; startRotRef.current = { ...rotationRef.current }; startPosRef.current = { x: evt.clientX, y: evt.clientY }; }, onDrag: ({ event, last, velocity = [0, 0], direction = [0, 0], movement }) => { if (focusedElRef.current || !draggingRef.current || !startPosRef.current) return; const evt = event; const dxTotal = evt.clientX - startPosRef.current.x; const dyTotal = evt.clientY - startPosRef.current.y; if (!movedRef.current) { const dist2 = dxTotal * dxTotal + dyTotal * dyTotal; if (dist2 > 16) movedRef.current = true; } const nextX = clamp(startRotRef.current.x - dyTotal / 20, -5, 5); const nextY = wrapAngleSigned(startRotRef.current.y + dxTotal / 20); if (rotationRef.current.x !== nextX || rotationRef.current.y !== nextY) { rotationRef.current = { x: nextX, y: nextY }; applyTransform(nextX, nextY); } if (last) { draggingRef.current = false; let [vMagX, vMagY] = velocity; const [dirX, dirY] = direction; let vx = vMagX * dirX; let vy = vMagY * dirY; if (Math.abs(vx) > 0.005 || Math.abs(vy) > 0.005) startInertia(vx, vy); if (movedRef.current) lastDragEndAt.current = performance.now(); movedRef.current = false; } } }, { target: mainRef, eventOptions: { passive: true } });
  useEffect(() => () => { document.body.classList.remove('dg-scroll-lock'); }, []);
  return (
    React.createElement('div', { ref: rootRef, className: 'sphere-root', style: { ['--segments-x']: segments, ['--segments-y']: segments, ['--overlay-blur-color']: '#060010', ['--tile-radius']: '30px', ['--enlarge-radius']: '30px', ['--image-filter']: 'grayscale(1)' } },
      React.createElement('main', { ref: mainRef, className: 'sphere-main' },
        React.createElement('div', { className: 'stage' },
          React.createElement('div', { ref: sphereRef, className: 'sphere' },
            items.map((it, i) => (
              React.createElement('div', { key: `${it.x},${it.y},${i}`, className: 'item', 'data-src': it.src, 'data-offset-x': it.x, 'data-offset-y': it.y, 'data-size-x': it.sizeX, 'data-size-y': it.sizeY, style: { ['--offset-x']: it.x, ['--offset-y']: it.y, ['--item-size-x']: it.sizeX, ['--item-size-y']: it.sizeY } },
                React.createElement('div', { className: 'item__image' }, React.createElement('img', { src: it.src, draggable: false, alt: it.alt }))
              )
            ))
          )
        ),
        React.createElement('div', { className: 'overlay' }),
        React.createElement('div', { className: 'overlay overlay--blur' }),
        React.createElement('div', { className: 'edge-fade edge-fade--top' }),
        React.createElement('div', { className: 'edge-fade edge-fade--bottom' }),
        React.createElement('div', { className: 'viewer' }, React.createElement('div', { ref: scrimRef, className: 'scrim' }), React.createElement('div', { ref: frameRef, className: 'frame' }))
      )
    )
  );
}

const style = document.createElement('style');
style.textContent = `
.sphere-root{position:relative;width:100%;height:100%;--radius:520px;--viewer-pad:72px;--circ:calc(var(--radius)*3.14);--rot-y:calc((360deg/var(--segments-x))/2);--rot-x:calc((360deg/var(--segments-y))/2);--item-width:calc(var(--circ)/var(--segments-x));--item-height:calc(var(--circ)/var(--segments-y))}.sphere-root *{box-sizing:border-box}.sphere,.item,.item__image{transform-style:preserve-3d}main.sphere-main{position:absolute;inset:0;display:grid;place-items:center;overflow:hidden;touch-action:none;user-select:none;-webkit-user-select:none;background:transparent}.stage{width:100%;height:100%;display:grid;place-items:center;perspective:calc(var(--radius)*2);perspective-origin:50% 50%;contain:layout paint size}.sphere{transform:translateZ(calc(var(--radius) * -1));will-change:transform}.overlay,.overlay--blur{position:absolute;inset:0;margin:auto;z-index:3;pointer-events:none}.overlay{background-image:radial-gradient(rgba(235,235,235,0) 65%, var(--overlay-blur-color, #060010) 100%)}.overlay--blur{-webkit-mask-image:radial-gradient(rgba(235,235,235,0) 70%, var(--overlay-blur-color, #060010) 90%);mask-image:radial-gradient(rgba(235,235,235,0) 70%, var(--overlay-blur-color, #060010) 90%);backdrop-filter:blur(3px)}.item{width:calc(var(--item-width) * var(--item-size-x));height:calc(var(--item-height) * var(--item-size-y));position:absolute;top:-999px;bottom:-999px;left:-999px;right:-999px;margin:auto;transform-origin:50% 50%;backface-visibility:hidden;transition:transform 300ms;transform:rotateY(calc(var(--rot-y) * (var(--offset-x) + ((var(--item-size-x) - 1) / 2)) + var(--rot-y-delta, 0deg))) rotateX(calc(var(--rot-x) * (var(--offset-y) - ((var(--item-size-y) - 1) / 2)) + var(--rot-x-delta, 0deg))) translateZ(var(--radius))}.item__image{position:absolute;display:block;inset:10px;border-radius:var(--tile-radius, 12px);background:transparent;overflow:hidden;backface-visibility:hidden;transition:transform 300ms;cursor:pointer;-webkit-tap-highlight-color:transparent;touch-action:manipulation;pointer-events:auto;-webkit-transform:translateZ(0);transform:translateZ(0)}.item__image:focus{outline:none}.item__image img{width:100%;height:100%;object-fit:cover;pointer-events:none;backface-visibility:hidden;filter:var(--image-filter, none)}.viewer{position:absolute;inset:0;z-index:20;pointer-events:none;display:flex;align-items:center;justify-content:center;padding:var(--viewer-pad)}.viewer .frame{height:100%;aspect-ratio:1;border-radius:var(--enlarge-radius, 32px);display:flex}@media (max-aspect-ratio:1/1){.viewer .frame{height:auto;width:100%}}.viewer .scrim{position:absolute;inset:0;z-index:10;background:rgba(0,0,0,.4);pointer-events:none;opacity:0;transition:opacity 500ms ease;backdrop-filter:blur(3px)}.sphere-root[data-enlarging='true'] .viewer .scrim{opacity:1;pointer-events:all}.viewer .enlarge{position:absolute;z-index:30;border-radius:var(--enlarge-radius, 32px);overflow:hidden;transition:transform 500ms ease, opacity 500ms ease;transform-origin:top left;box-shadow:0 10px 30px rgba(0,0,0,.35)}.viewer .enlarge img{width:100%;height:100%;object-fit:cover;filter:var(--image-filter, none)}.sphere-root .enlarge-closing img{filter:var(--image-filter, none)}.edge-fade{position:absolute;left:0;right:0;height:120px;z-index:5;pointer-events:none;background:linear-gradient(to bottom, transparent, var(--overlay-blur-color, #060010))}.edge-fade--top{top:0;transform:rotate(180deg)}.edge-fade--bottom{bottom:0}`;
document.head.appendChild(style);

export function mountDomeGallery(rootId = 'dome-gallery-root') {
  const mount = document.getElementById(rootId);
  if (!mount) return;
  const root = createRoot(mount);
  root.render(React.createElement(DomeGallery, {}));
}

// Auto-mount on load
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => mountDomeGallery());
} else {
  mountDomeGallery();
}


