// RF-10 (sucursal): existencias por talla de MI sucursal.
document.addEventListener('DOMContentLoaded', async () => {
    if (!await Admin.iniciar()) { return; }
    const cuerpo = document.getElementById('cuerpo');
    const selP = document.getElementById('sel-producto');
    const selT = document.getElementById('sel-talla');
    Admin.TALLAS.forEach(t => selT.appendChild(new Option(t, t)));

    const cargar = async () => {
        const d = await Admin.get('inventario');
        document.getElementById('titulo').textContent = 'Inventario · ' + d.sucursal;
        cuerpo.innerHTML = ''; selP.innerHTML = '';
        if (!d.productos.length) {
            const r = cuerpo.insertRow(); const c = r.insertCell(); c.colSpan = 3;
            c.appendChild(document.createTextNode('Tu sucursal aún no tiene inventario. Da de alta productos en '));
            const a = Admin.el('a', '', 'Productos'); a.href = 'productos.html'; c.appendChild(a); c.appendChild(document.createTextNode('.'));
        }
        d.productos.forEach(p => {
            if (p.activo) { selP.appendChild(new Option(p.nombre, p.id)); }
            p.tallas.forEach((t, k) => {
                const r = cuerpo.insertRow();
                r.className = (p.activo ? '' : 'inactivo ') + (t.existencias === 0 ? 'agotado' : '');
                if (k === 0) {
                    const c = r.insertCell(); c.rowSpan = p.tallas.length; c.id = 'p' + p.id;
                    c.appendChild(document.createTextNode(p.nombre));
                    if (!p.activo) { c.appendChild(Admin.el('small', '', ' (desactivado)')); }
                }
                r.insertCell().textContent = t.talla === '' ? 'Única' : t.talla;
                const i = document.createElement('input'); i.className = 'numero'; i.type = 'number'; i.min = 0; i.value = t.existencias;
                i.dataset.idProducto = p.id; i.dataset.talla = t.talla;
                r.insertCell().appendChild(i);
            });
        });
        if (location.hash) { document.querySelector(location.hash)?.scrollIntoView({ behavior: 'smooth' }); }
    };

    document.getElementById('form-inventario').addEventListener('submit', async (e) => {
        e.preventDefault();
        const existencias = [...cuerpo.querySelectorAll('input[data-id-producto]')].map(i => ({
            idProducto: parseInt(i.dataset.idProducto, 10), talla: i.dataset.talla, cantidad: Math.max(0, parseInt(i.value, 10) || 0)
        }));
        if (await Admin.post('inventario', { accion: 'guardar', existencias }, '¿Guardar las existencias capturadas?')) { cargar(); }
    });
    const ft = document.getElementById('form-talla');
    ft.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!selP.value) { alert('Primero da de alta un producto en tu sucursal.'); return; }
        if (await Admin.post('inventario', { accion: 'agregar', idProducto: parseInt(selP.value, 10), talla: selT.value, cantidad: parseInt(ft.cantidad.value, 10) || 0 }, '¿Agregar esta talla al inventario?')) { cargar(); }
    });
    cargar();
});
