function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function formatQuantity(value) {
  return new Intl.NumberFormat('es-SV', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4,
  }).format(Number(value) || 0);
}

function formatDate(value) {
  if (!value) {
    return 'Sin fecha';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('es-SV', {
    dateStyle: 'medium',
  }).format(parsedDate);
}

function formatDateTime(value) {
  if (!value) {
    return 'Sin fecha';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('es-SV', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(parsedDate);
}

function renderMetaCard(label, value) {
  return `
    <div class="meta-card">
      <div class="meta-label">${escapeHtml(label)}</div>
      <div class="meta-value">${escapeHtml(value || 'Sin dato')}</div>
    </div>
  `;
}

function buildPrintDocumentHtml(title, bodyHtml) {
  return `
    <!doctype html>
    <html lang="es">
      <head>
        <meta charset="utf-8" />
        <title>${escapeHtml(title)}</title>
        <style>
          :root {
            color-scheme: light;
            --ink: #173d2c;
            --copy: #35594a;
            --soft: #718779;
            --line: #d9e4dc;
            --surface: #f7faf8;
            --accent: #2e8b57;
            --alert: rgba(23, 61, 44, 0.08);
          }

          * {
            box-sizing: border-box;
          }

          body {
            margin: 0;
            padding: 32px;
            font-family: "Segoe UI", "Helvetica Neue", Arial, sans-serif;
            color: var(--ink);
            background: white;
          }

          .sheet {
            position: relative;
            max-width: 1024px;
            margin: 0 auto;
          }

          .watermark {
            position: absolute;
            inset: 160px 0 auto 0;
            text-align: center;
            font-size: 64px;
            font-weight: 900;
            letter-spacing: 0.28em;
            color: rgba(23, 61, 44, 0.06);
            transform: rotate(-18deg);
            pointer-events: none;
            user-select: none;
          }

          .header {
            display: flex;
            justify-content: space-between;
            gap: 24px;
            align-items: flex-start;
            border-bottom: 2px solid var(--ink);
            padding-bottom: 18px;
          }

          .eyebrow {
            font-size: 11px;
            font-weight: 800;
            letter-spacing: 0.24em;
            text-transform: uppercase;
            color: var(--soft);
          }

          .title {
            margin: 12px 0 0;
            font-size: 34px;
            line-height: 1.05;
            letter-spacing: -0.05em;
          }

          .subtitle {
            margin: 12px 0 0;
            font-size: 14px;
            line-height: 1.6;
            color: var(--copy);
          }

          .meta-grid {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 12px;
            margin-top: 20px;
          }

          .meta-card {
            border: 1px solid var(--line);
            border-radius: 14px;
            padding: 12px 14px;
            background: var(--surface);
          }

          .meta-label {
            font-size: 11px;
            font-weight: 800;
            letter-spacing: 0.18em;
            text-transform: uppercase;
            color: var(--soft);
          }

          .meta-value {
            margin-top: 8px;
            font-size: 14px;
            font-weight: 700;
            line-height: 1.5;
          }

          .section {
            margin-top: 26px;
          }

          .section-title {
            margin: 0 0 12px;
            font-size: 16px;
            font-weight: 900;
            letter-spacing: -0.02em;
          }

          .notes {
            margin-top: 10px;
            border: 1px solid var(--line);
            border-radius: 14px;
            padding: 12px 14px;
            background: var(--surface);
            font-size: 13px;
            line-height: 1.6;
            color: var(--copy);
          }

          table {
            width: 100%;
            border-collapse: collapse;
          }

          th, td {
            border: 1px solid var(--line);
            padding: 10px 12px;
            vertical-align: top;
          }

          th {
            background: var(--surface);
            font-size: 11px;
            font-weight: 900;
            letter-spacing: 0.16em;
            text-transform: uppercase;
            text-align: left;
            color: var(--soft);
          }

          td {
            font-size: 13px;
            line-height: 1.55;
          }

          .muted {
            color: var(--soft);
          }

          .batch-list {
            margin: 0;
            padding-left: 18px;
          }

          .footer {
            margin-top: 56px;
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px;
          }

          .signature {
            border-top: 1px solid var(--ink);
            padding-top: 12px;
            min-height: 72px;
          }

          @page {
            size: A4 portrait;
            margin: 14mm;
          }

          @media print {
            body {
              padding: 0;
            }
          }
        </style>
      </head>
      <body>${bodyHtml}</body>
    </html>
  `;
}

function createPrintSession(title) {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    throw new Error('La impresion solo esta disponible en el navegador.');
  }

  const iframe = document.createElement('iframe');
  iframe.setAttribute('title', title);
  iframe.setAttribute('aria-hidden', 'true');
  iframe.style.position = 'fixed';
  iframe.style.right = '0';
  iframe.style.bottom = '0';
  iframe.style.width = '1px';
  iframe.style.height = '1px';
  iframe.style.opacity = '0';
  iframe.style.pointerEvents = 'none';
  iframe.style.border = '0';

  document.body.appendChild(iframe);

  return {
    iframe,
    cleanupTimeoutId: null,
    loadHandler: null,
    disposed: false,
  };
}

function disposePrintSession(session) {
  if (!session || session.disposed) {
    return;
  }

  session.disposed = true;

  if (session.cleanupTimeoutId) {
    window.clearTimeout(session.cleanupTimeoutId);
    session.cleanupTimeoutId = null;
  }

  if (session.iframe && session.loadHandler) {
    session.iframe.removeEventListener('load', session.loadHandler);
    session.loadHandler = null;
  }

  if (session.iframe?.parentNode) {
    session.iframe.parentNode.removeChild(session.iframe);
  }
}

function writePrintSession(session, title, bodyHtml, shouldPrint = false) {
  if (!session || session.disposed || !session.iframe?.isConnected) {
    throw new Error('La sesion de impresion ya no esta disponible.');
  }

  if (session.loadHandler) {
    session.iframe.removeEventListener('load', session.loadHandler);
    session.loadHandler = null;
  }

  if (shouldPrint) {
    session.loadHandler = () => {
      session.iframe.removeEventListener('load', session.loadHandler);
      session.loadHandler = null;

      const frameWindow = session.iframe.contentWindow;

      if (!frameWindow) {
        disposePrintSession(session);
        return;
      }

      const cleanup = () => disposePrintSession(session);

      frameWindow.addEventListener('afterprint', cleanup, { once: true });

      window.setTimeout(() => {
        try {
          frameWindow.focus();
          frameWindow.print();
        } catch (error) {
          cleanup();
          throw error;
        }
      }, 50);

      session.cleanupTimeoutId = window.setTimeout(cleanup, 60000);
    };

    session.iframe.addEventListener('load', session.loadHandler);
  }

  session.iframe.srcdoc = buildPrintDocumentHtml(title, bodyHtml);
}

export function openPrintPreviewWindow(title) {
  const printSession = createPrintSession(title);

  writePrintSession(
    printSession,
    title,
    `
      <main class="sheet">
        <header class="header">
          <div>
            <div class="eyebrow">Preparando impresion</div>
            <h1 class="title">${escapeHtml(title)}</h1>
            <p class="subtitle">Generando el documento imprimible desde el backend...</p>
          </div>
        </header>
      </main>
    `,
  );

  return printSession;
}

export function renderPrintWindowError(printSession) {
  if (!printSession) {
    return;
  }

  disposePrintSession(printSession);
}

function openPrintWindow(title, bodyHtml, existingSession) {
  const printSession = existingSession ?? openPrintPreviewWindow(title);
  writePrintSession(printSession, title, bodyHtml, true);
}

export function printRecipeDocument(document, existingWindow) {
  const rows = document.items
    .map(
      (item) => `
        <tr>
          <td>${escapeHtml(item.itemOrder)}</td>
          <td>
            <strong>${escapeHtml(item.productName)}</strong><br />
            <span class="muted">${escapeHtml(item.productCode)}</span>
          </td>
          <td>${escapeHtml(item.unitOfMeasureSymbol || item.unitOfMeasureName)}</td>
          <td>${escapeHtml(formatQuantity(item.theoreticalQuantity))}</td>
          <td>${escapeHtml(item.observations || 'Sin observaciones')}</td>
        </tr>
      `,
    )
    .join('');

  openPrintWindow(
    `Formula ${document.recipeCode}`,
    `
      <main class="sheet">
        <header class="header">
          <div>
            <div class="eyebrow">Formula Teorica</div>
            <h1 class="title">${escapeHtml(document.recipeName)}</h1>
            <p class="subtitle">
              ${escapeHtml(document.manufacturedProductName)} (${escapeHtml(document.manufacturedProductCode)})
            </p>
          </div>
          <div class="eyebrow">Generado ${escapeHtml(formatDateTime(document.generatedAt))}</div>
        </header>

        <section class="meta-grid">
          ${renderMetaCard('Producto', `${document.manufacturedProductName} (${document.manufacturedProductCode})`)}
          ${renderMetaCard('Grupo', document.groupCode)}
          ${renderMetaCard('Ciclo', document.cycle)}
          ${renderMetaCard('Lote', document.lotNumber)}
        </section>

        <section class="section">
          <h2 class="section-title">Materias primas y cantidades teoricas</h2>
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Materia prima</th>
                <th>Unidad</th>
                <th>Cantidad teorica</th>
                <th>Observaciones</th>
              </tr>
            </thead>
            <tbody>${rows}</tbody>
          </table>
        </section>

        <section class="footer">
          <div class="signature">
            <strong>Responsable de preparacion</strong>
          </div>
          <div class="signature">
            <strong>Revision docente</strong>
          </div>
        </section>
      </main>
    `,
    existingWindow,
  );
}

export function printProductionRunDocument(document, existingWindow) {
  const rows = document.items
    .map(
      (item) => `
        <tr>
          <td>${escapeHtml(item.itemOrder)}</td>
          <td>
            <strong>${escapeHtml(item.productName)}</strong><br />
            <span class="muted">${escapeHtml(item.productCode)}</span>
          </td>
          <td>${escapeHtml(item.unitOfMeasureSymbol || item.unitOfMeasureName)}</td>
          <td>${escapeHtml(formatQuantity(item.theoreticalQuantity))}</td>
          <td>${escapeHtml(item.actualQuantity == null ? 'Pendiente' : formatQuantity(item.actualQuantity))}</td>
          <td>${escapeHtml(item.observations || 'Sin observaciones')}</td>
          <td>
            ${
              item.allocations.length
                ? `<ul class="batch-list">${item.allocations
                    .map(
                      (allocation) => `
                        <li>
                          ${escapeHtml(allocation.batchCode)} · ${escapeHtml(formatQuantity(allocation.actualQuantity))}
                          ${allocation.expirationDate ? ` · vence ${escapeHtml(formatDate(allocation.expirationDate))}` : ''}
                        </li>
                      `,
                    )
                    .join('')}</ul>`
                : '<span class="muted">Sin detalle por lote</span>'
            }
          </td>
        </tr>
      `,
    )
    .join('');

  openPrintWindow(
    `Descargo ${document.recipeCode}`,
    `
      <main class="sheet">
        <div class="watermark">${escapeHtml(document.controlMark || 'DOCUMENTO DE CONTROL')}</div>
        <header class="header">
          <div>
            <div class="eyebrow">Descargo Real</div>
            <h1 class="title">${escapeHtml(document.manufacturedProductName)}</h1>
            <p class="subtitle">
              Formula ${escapeHtml(document.recipeCode)} / ${escapeHtml(document.recipeName)}
            </p>
          </div>
          <div class="eyebrow">Generado ${escapeHtml(formatDateTime(document.generatedAt))}</div>
        </header>

        <section class="meta-grid">
          ${renderMetaCard('Producto', `${document.manufacturedProductName} (${document.manufacturedProductCode})`)}
          ${renderMetaCard('Grupo', document.groupName)}
          ${renderMetaCard('Ciclo', document.cycle)}
          ${renderMetaCard('Lote', document.lotNumber)}
          ${renderMetaCard('Laboratorio', document.laboratoryName)}
          ${renderMetaCard('Fecha de laboratorio', formatDateTime(document.laboratoryDate))}
          ${renderMetaCard('Preparado por', document.preparedByUsername)}
          ${renderMetaCard('Confirmado por', document.confirmedByUsername || 'Pendiente')}
        </section>

        <section class="section">
          <h2 class="section-title">Materias primas, teoria y consumo real</h2>
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Materia prima</th>
                <th>Unidad</th>
                <th>Cantidad teorica</th>
                <th>Cantidad real</th>
                <th>Observaciones</th>
                <th>Lotes aplicados</th>
              </tr>
            </thead>
            <tbody>${rows}</tbody>
          </table>
        </section>

        <section class="section">
          <h2 class="section-title">Observaciones generales</h2>
          <div class="notes">${escapeHtml(document.notes || 'Sin observaciones adicionales.')}</div>
        </section>

        <section class="footer">
          <div class="signature">
            <strong>Responsable del descargo</strong>
          </div>
          <div class="signature">
            <strong>Autorizacion docente</strong>
          </div>
        </section>
      </main>
    `,
    existingWindow,
  );
}
