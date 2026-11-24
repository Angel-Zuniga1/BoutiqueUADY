document.addEventListener('DOMContentLoaded', async () => {
    if (!await Admin.iniciar()) { return; }
    const id = new URLSearchParams(location.search).get('id');
    let d;
    try { d = await Admin.get('sucursales?id=' + encodeURIComponent(id)); }
    catch (e) { alert(e.message); location.replace('sucursales.html'); return; }

    document.getElementById('titulo').textContent = 'Sucursal ' + d.nombre;
    document.title = 'Sucursal ' + d.nombre + ' | Admin Boutique UADY';
    const tarjetas = document.getElementById('tarjetas');
    const tarjeta = (v, t) => { const x = Admin.el('div', 'resumen'); x.appendChild(Admin.el('b', '', v)); x.appendChild(Admin.el('span', '', t)); tarjetas.appendChild(x); };
    tarjeta(Admin.dinero(d.resumen.ventas), 'Ventas (no canceladas)');
    tarjeta(d.resumen.pedidos, 'Pedidos en total');
    tarjeta(d.resumen.porEstado['pendiente'], 'Pendientes');
    tarjeta(d.inventario.length, 'Productos en inventario');
    tarjeta(Admin.dinero(d.valorInventario), 'Valor del inventario');

    const cab = document.getElementById('estados-cab'), fila = document.getElementById('estados-fila');
    Admin.ESTADOS.forEach(e => { cab.appendChild(Admin.el('th', '', Admin.cap(e))); fila.appendChild(Admin.el('td', '', d.resumen.porEstado[e])); });

    const mv = document.getElementById('mas-vendidos');
    if (!d.masVendidos.length) { mv.appendChild(Admin.el('p', '', 'Aún no hay ventas.')); }
    else {
        const t = Admin.el('table', 'tabla-admin'); const th = t.createTHead().insertRow();
        ['Producto', 'Unidades', 'Importe'].forEach(x => th.appendChild(Admin.el('th', '', x)));
        const tb = t.createTBody();
        d.masVendidos.forEach(m => { const r = tb.insertRow(); r.insertCell().textContent = m.nombre; r.insertCell().textContent = m.unidades; r.insertCell().textContent = Admin.dinero(m.importe); });
        mv.appendChild(t);
    }

    document.getElementById('titulo-inv').textContent = 'Inventario de ' + d.nombre;
    const inv = document.getElementById('inventario');
    if (!d.inventario.length) { const r = inv.insertRow(); const c = r.insertCell(); c.colSpan = 4; c.textContent = 'Esta sucursal aún no tiene inventario.'; }
    d.inventario.forEach(p => p.tallas.forEach((t, k) => {
        const r = inv.insertRow();
        r.className = (p.activo ? '' : 'inactivo ') + (t.existencias === 0 ? 'agotado' : '');
        if (k === 0) { const c1 = r.insertCell(); c1.rowSpan = p.tallas.length; c1.textContent = p.nombre; const c2 = r.insertCell(); c2.rowSpan = p.tallas.length; c2.textContent = Admin.dinero(p.precio); }
        r.insertCell().textContent = t.talla === '' ? 'Única' : t.talla;
        r.insertCell().textContent = t.existencias;
    }));

    const ped = document.getElementById('pedidos');
    if (!d.pedidos.length) { const r = ped.insertRow(); const c = r.insertCell(); c.colSpan = 5; c.textContent = 'Sin pedidos.'; }
    d.pedidos.forEach(p => { const r = ped.insertRow(); [p.idPedido, p.fecha, p.cliente, Admin.dinero(p.total), p.estado].forEach(v => r.insertCell().textContent = v); });
});
