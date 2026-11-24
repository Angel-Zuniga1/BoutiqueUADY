document.addEventListener('DOMContentLoaded', async () => {
    if (!await Admin.iniciar()) { return; }
    const cuerpo = document.getElementById('cuerpo');
    const cargar = async () => {
        const lista = await Admin.get('sucursales');
        document.getElementById('titulo-lista').textContent = 'Sucursales (' + lista.length + ')';
        cuerpo.innerHTML = '';
        lista.forEach(s => {
            const tr = cuerpo.insertRow();
            if (!s.activa) { tr.className = 'inactivo'; }
            tr.insertCell().textContent = s.id;
            tr.insertCell().textContent = s.nombre;
            tr.insertCell().textContent = s.activa ? 'Activa' : 'Inactiva';
            const ca = tr.insertCell();
            if (s.admins) { ca.textContent = s.admins; } else { ca.appendChild(Admin.el('small', '', 'Sin administrador')); }
            tr.insertCell().textContent = s.productos;
            tr.insertCell().textContent = s.pedidos + ' (' + s.pendientes + ' pend.)';
            tr.insertCell().textContent = Admin.dinero(s.ventas);
            const acc = tr.insertCell(); acc.className = 'acciones';
            const ver = Admin.el('a', 'boton chico', 'Inventario y ventas'); ver.href = 'sucursal_detalle.html?id=' + s.id;
            const btn = Admin.el('button', 'boton chico' + (s.activa ? ' peligro' : ''), s.activa ? 'Desactivar' : 'Activar');
            btn.type = 'button';
            btn.addEventListener('click', async () => {
                if (await Admin.post('sucursales', { accion: s.activa ? 'desactivar' : 'activar', id: s.id },
                        '¿' + (s.activa ? 'Desactivar' : 'Activar') + ' la sucursal ' + s.nombre + '?')) { cargar(); }
            });
            acc.append(ver, btn);
        });
    };
    const form = document.getElementById('form-crear');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!Admin.validar(form)) { return; }
        if (await Admin.post('sucursales', { accion: 'crear', nombre: form.nombre.value.trim() }, '¿Agregar esta sucursal?')) { form.reset(); cargar(); }
    });
    cargar();
});
