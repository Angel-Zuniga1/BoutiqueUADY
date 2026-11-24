document.addEventListener('DOMContentLoaded', async () => {
    if (!await Admin.iniciar()) { return; }
    const cuerpo = document.getElementById('cuerpo');
    const sel = document.getElementById('sel-sucursal');
    const form = document.getElementById('form-crear');

    const cargar = async () => {
        const d = await Admin.get('administradores');
        sel.innerHTML = '';
        d.sucursales.forEach(s => sel.appendChild(new Option(s.nombre, s.id)));
        document.getElementById('sin-sucursales').hidden = d.sucursales.length > 0;
        cuerpo.innerHTML = '';
        d.administradores.forEach(a => {
            const tr = cuerpo.insertRow();
            if (!a.activo) { tr.className = 'inactivo'; }
            [a.idAdmin, a.nombre, a.correo, a.rol === 'principal' ? 'Principal' : 'Sucursal', a.sucursal || '—', a.activo ? 'Activa' : 'Desactivada']
                .forEach(v => tr.insertCell().textContent = v);
            const acc = tr.insertCell(); acc.className = 'acciones';
            if (a.rol !== 'sucursal') { acc.textContent = '—'; return; }
            const btn = Admin.el('button', 'boton chico' + (a.activo ? ' peligro' : ''), a.activo ? 'Desactivar' : 'Activar');
            btn.type = 'button';
            btn.addEventListener('click', async () => {
                if (await Admin.post('administradores', { accion: a.activo ? 'desactivar' : 'activar', id: a.idAdmin },
                        '¿' + (a.activo ? 'Desactivar' : 'Activar') + ' la cuenta de ' + a.nombre + '?')) { cargar(); }
            });
            const fp = Admin.el('form', 'inline'); fp.autocomplete = 'off';
            const inp = document.createElement('input'); inp.type = 'password'; inp.name = 'password'; inp.placeholder = 'Nueva contraseña'; inp.minLength = 6; inp.required = true; inp.className = 'chico';
            const bp = Admin.el('button', 'boton chico', 'Cambiar'); bp.type = 'submit';
            fp.append(inp, bp);
            fp.addEventListener('submit', async (e) => {
                e.preventDefault();
                if (!Admin.validar(fp)) { return; }
                if (await Admin.post('administradores', { accion: 'password', id: a.idAdmin, password: inp.value }, '¿Cambiar la contraseña de ' + a.nombre + '?')) { inp.value = ''; }
            });
            acc.append(btn, fp);
        });
    };
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!Admin.validar(form)) { return; }
        const datos = Object.fromEntries(new FormData(form));
        datos.accion = 'crear'; datos.idSucursal = parseInt(datos.idSucursal, 10);
        if (await Admin.post('administradores', datos, '¿Crear esta cuenta de administrador de sucursal?')) { form.reset(); cargar(); }
    });
    cargar();
});
