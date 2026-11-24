document.addEventListener('DOMContentLoaded', async () => {
    const s = await Admin.iniciar();
    if (!s) { return; }
    // Aviso de bienvenida que manda login.html
    const p = new URLSearchParams(location.search);
    if (p.get('ok')) { alert(p.get('ok')); history.replaceState(null, '', 'index.html'); }

    const tarjetas = document.getElementById('tarjetas');
    const tarjeta = (valor, texto, href) => {
        const t = Admin.el(href ? 'a' : 'div', 'resumen');
        if (href) { t.href = href; }
        t.appendChild(Admin.el('b', '', valor));
        t.appendChild(Admin.el('span', '', texto));
        tarjetas.appendChild(t);
    };
    const r = await Admin.get('resumen');
    const titulo = document.getElementById('titulo-rol'), lista = document.getElementById('lista-rol');
    const li = (b, t) => { const l = document.createElement('li'); if (b) { l.appendChild(Admin.el('b', '', b + ' ')); } l.appendChild(document.createTextNode(t)); lista.appendChild(l); };

    if (s.rol === 'principal') {
        tarjeta(Admin.dinero(r.ventas), 'Ventas globales (no canceladas)');
        tarjeta(r.pendientes, 'Pedidos pendientes en total', 'sucursales.html');
        tarjeta(r.sucursalesActivas, 'Sucursales activas', 'sucursales.html');
        tarjeta(r.admins, 'Admins. de sucursal activos', 'administradores.html');
        document.getElementById('por-sucursal').hidden = false;
        const cuerpo = document.getElementById('cuerpo-sucursales');
        r.porSucursal.forEach(f => {
            const tr = cuerpo.insertRow();
            if (!f.activa) { tr.className = 'inactivo'; }
            [f.nombre, f.activa ? 'Activa' : 'Inactiva', f.pedidos, f.porEstado['pendiente'], f.porEstado['listo para recoger'],
             f.porEstado['entregado'], f.porEstado['cancelado'], Admin.dinero(f.ventas)].forEach(v => tr.insertCell().textContent = v);
            const a = Admin.el('a', 'boton chico', 'Ver inventario y ventas'); a.href = 'sucursal_detalle.html?id=' + f.id;
            tr.insertCell().appendChild(a);
        });
        titulo.textContent = 'Tu papel como administrador principal';
        li('Sucursales:', 'ver y agregar sucursales, y consultar el inventario y las ventas de cada una.');
        li('Administradores:', 'crear las cuentas de los administradores de sucursal y administrarlas.');
        li('', 'El alta de prendas, las tallas, las existencias y los pedidos los lleva cada administrador de sucursal.');
    } else {
        tarjeta(r.porEstado['pendiente'], 'Pedidos pendientes', 'pedidos.html?estado=pendiente');
        tarjeta(r.porEstado['listo para recoger'], 'Listos para recoger', 'pedidos.html?estado=listo%20para%20recoger');
        tarjeta(r.agotados, 'Tallas agotadas', 'inventario.html');
        tarjeta(r.productos, 'Productos en tu sucursal', 'productos.html');
        tarjeta(Admin.dinero(r.ventas), 'Ventas de tu sucursal');
        titulo.textContent = 'Qué administras en ' + r.sucursal;
        li('Productos:', 'dar de alta prendas y accesorios en tu sucursal, modificarlos y darlos de baja (RF-09).');
        li('Inventario:', 'existencias por talla de tu sucursal (RF-10).');
        li('Pedidos:', 'ventas de tu sucursal y su estado: pendiente, listo para recoger, entregado o cancelado (RF-11).');
    }
});
