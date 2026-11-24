// RF-09 (sucursal): productos de MI sucursal.
document.addEventListener('DOMContentLoaded', async () => {
    const s = await Admin.iniciar();
    if (!s) { return; }
    const form = document.getElementById('form-producto');
    const cuerpo = document.getElementById('cuerpo');
    const selCat = document.getElementById('sel-categoria');
    const casilla = document.getElementById('con-tallas');
    const porTallas = document.getElementById('tallas-iniciales');
    const unica = document.getElementById('unica-inicial');
    let productos = [];

    Admin.TALLAS.forEach(t => {
        const l = Admin.el('label', 'talla-inicial', t + ' ');
        const i = document.createElement('input'); i.className = 'numero'; i.type = 'number'; i.min = 0; i.name = 'inicial_' + t; i.value = 0;
        l.appendChild(i); porTallas.appendChild(l);
    });
    const ajustarTallas = () => { porTallas.hidden = !casilla.checked; unica.hidden = casilla.checked; };
    casilla.addEventListener('change', ajustarTallas); ajustarTallas();

    const modoEditar = (p) => {
        form.accion.value = p ? 'editar' : 'crear';
        form.id.value = p ? p.id : '';
        form.nombre.value = p ? p.nombre : '';
        form.descripcion.value = p ? (p.descripcion || '') : '';
        form.precio.value = p ? p.precio : '';
        form.imagen.value = p ? (p.imagen || '') : '';
        if (p) { selCat.value = p.idCategoria; }
        form.archivo.value = '';
        document.getElementById('bloque-inicial').hidden = !!p;
        document.getElementById('btn-cancelar').hidden = !p;
        document.getElementById('nota-editar').hidden = !p;
        document.getElementById('btn-guardar').textContent = p ? 'Guardar cambios' : 'Dar de alta';
        document.getElementById('titulo-form').textContent = p ? 'Modificar producto #' + p.id : 'Dar de alta un producto nuevo en ' + s.sucursal;
        if (p) { window.scrollTo({ top: 0, behavior: 'smooth' }); }
    };

    const cargar = async () => {
        const d = await Admin.get('productos');
        productos = d.productos;
        document.getElementById('titulo').textContent = 'Productos · ' + d.sucursal;
        document.getElementById('titulo-form').textContent = 'Dar de alta un producto nuevo en ' + d.sucursal;
        selCat.innerHTML = '';
        d.categorias.forEach(c => selCat.appendChild(new Option(c.nombre, c.id)));
        document.getElementById('titulo-lista').textContent = 'Productos en ' + d.sucursal + ' (' + d.productos.length + ')';
        cuerpo.innerHTML = '';
        if (!d.productos.length) { const r = cuerpo.insertRow(); const c = r.insertCell(); c.colSpan = 8; c.textContent = 'Tu sucursal aún no tiene productos. Da de alta uno arriba.'; }
        d.productos.forEach(p => {
            const tr = cuerpo.insertRow();
            if (p.existencias === 0) { tr.className = 'agotado'; }
            tr.insertCell().textContent = p.id;
            const img = document.createElement('img'); img.className = 'mini'; img.src = '../' + p.imagen; img.alt = '';
            tr.insertCell().appendChild(img);
            const cn = tr.insertCell(); cn.appendChild(document.createTextNode(p.nombre)); cn.appendChild(document.createElement('br')); cn.appendChild(Admin.el('small', '', p.descripcion || ''));
            tr.insertCell().textContent = p.categoria;
            tr.insertCell().textContent = Admin.dinero(p.precio);
            tr.insertCell().textContent = p.tallas > 1 ? p.tallas + ' tallas' : 'Única';
            tr.insertCell().textContent = p.existencias + (p.existencias === 0 ? ' (agotado)' : '');
            const acc = tr.insertCell(); acc.className = 'acciones';
            const bm = Admin.el('button', 'boton chico', 'Modificar'); bm.type = 'button'; bm.addEventListener('click', () => modoEditar(p));
            const be = Admin.el('a', 'boton chico secundario', 'Existencias'); be.href = 'inventario.html#p' + p.id;
            const bb = Admin.el('button', 'boton chico peligro', 'Dar de baja'); bb.type = 'button';
            bb.addEventListener('click', async () => {
                const fd = new FormData(); fd.append('accion', 'baja'); fd.append('id', p.id);
                if (await Admin.post('productos', fd, '¿Dar de baja "' + p.nombre + '" en ' + d.sucursal + '? Se quitará su inventario de esta sucursal.')) { cargar(); }
            });
            acc.append(bm, be, bb);
        });
        const otros = document.getElementById('sel-otros');
        otros.innerHTML = '';
        d.otros.forEach(o => otros.appendChild(new Option(o.nombre, o.id)));
        document.getElementById('bloque-otros').hidden = d.otros.length === 0;
    };

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!Admin.validar(form)) { return; }
        if (parseFloat(form.precio.value) <= 0) { alert('El precio debe ser mayor a cero.'); form.precio.focus(); return; }
        const editar = form.accion.value === 'editar';
        if (await Admin.post('productos', new FormData(form), editar ? '¿Guardar los cambios del producto?' : '¿Dar de alta este producto en tu sucursal?')) {
            form.reset(); modoEditar(null); ajustarTallas(); cargar();
        }
    });
    document.getElementById('btn-cancelar').addEventListener('click', (e) => { e.preventDefault(); form.reset(); modoEditar(null); ajustarTallas(); });
    const formOtros = document.getElementById('form-otros');
    formOtros.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(formOtros); fd.append('accion', 'alta_existente');
        if (await Admin.post('productos', fd, '¿Agregar este producto a tu sucursal?')) { location.href = 'inventario.html'; }
    });
    cargar();
});
