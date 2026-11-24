// RF-11 (sucursal): pedidos y ventas de MI sucursal, con cambio de estado.
document.addEventListener('DOMContentLoaded', async () => {
    if (!await Admin.iniciar()) { return; }
    const sel = document.getElementById('sel-estado');
    Admin.ESTADOS.forEach(e => sel.appendChild(new Option(Admin.cap(e), e)));
    sel.value = new URLSearchParams(location.search).get('estado') || '';
    const tarjetas = document.getElementById('tarjetas');
    const lista = document.getElementById('lista');

    const cargar = async () => {
        const d = await Admin.get('pedidos' + (sel.value ? '?estado=' + encodeURIComponent(sel.value) : ''));
        document.getElementById('titulo').textContent = 'Pedidos y ventas · ' + d.sucursal;
        tarjetas.innerHTML = '';
        const tarjeta = (v, t, estado) => {
            const x = Admin.el(estado !== undefined ? 'a' : 'div', 'resumen');
            if (estado !== undefined) { x.href = '#'; x.addEventListener('click', (e) => { e.preventDefault(); sel.value = estado; cargar(); }); }
            x.appendChild(Admin.el('b', '', v)); x.appendChild(Admin.el('span', '', t)); tarjetas.appendChild(x);
        };
        tarjeta(Admin.dinero(d.resumen.ventas), 'Ventas (no canceladas)');
        Admin.ESTADOS.forEach(e => tarjeta(d.resumen.porEstado[e], Admin.cap(e), e));

        document.getElementById('titulo-lista').textContent = d.pedidos.length + ' pedido(s)';
        lista.innerHTML = '';
        if (!d.pedidos.length) { lista.appendChild(Admin.el('p', '', 'No hay pedidos con ese filtro.')); }
        d.pedidos.forEach(p => {
            const art = Admin.el('article', 'pedido-admin estado-' + p.estado.replace(/ /g, '-'));
            const head = document.createElement('header');
            const info = document.createElement('div');
            info.appendChild(Admin.el('b', '', 'Pedido #' + p.idPedido));
            info.appendChild(document.createTextNode(' · ' + p.fecha));
            info.appendChild(document.createElement('br'));
            info.appendChild(Admin.el('small', '', p.cliente + ' · ' + p.correo + (p.telefono ? ' · ' + p.telefono : '')));
            head.append(info, Admin.el('div', 'total', Admin.dinero(p.total)));
            const ul = Admin.el('ul', 'renglones');
            p.detalles.forEach(dd => ul.appendChild(Admin.el('li', '', dd.cantidad + ' × ' + dd.nombre + (dd.talla ? ' (talla ' + dd.talla + ')' : '') + ' — ' + Admin.dinero(dd.cantidad * dd.precioUnitario))));
            const f = Admin.el('form', 'inline-form');
            const lab = Admin.el('label', '', 'Estado ');
            const s = document.createElement('select');
            Admin.ESTADOS.forEach(e => s.appendChild(new Option(Admin.cap(e), e)));
            s.value = p.estado; s.disabled = p.estado === 'entregado';
            lab.appendChild(s); f.appendChild(lab);
            if (p.estado !== 'entregado') {
                const b = Admin.el('button', 'boton chico', 'Cambiar estado'); b.type = 'submit'; f.appendChild(b);
                f.addEventListener('submit', async (e) => {
                    e.preventDefault();
                    if (await Admin.post('pedidos', { idPedido: p.idPedido, estado: s.value },
                            '¿Cambiar el estado del pedido #' + p.idPedido + '? Si lo cancelas, las existencias regresan al inventario.')) { cargar(); }
                });
            } else {
                f.appendChild(Admin.el('span', 'chip', 'Entregado'));
            }
            art.append(head, ul, f);
            lista.appendChild(art);
        });
    };
    sel.addEventListener('change', cargar);
    document.getElementById('form-filtro').addEventListener('submit', e => e.preventDefault());
    cargar();
});
