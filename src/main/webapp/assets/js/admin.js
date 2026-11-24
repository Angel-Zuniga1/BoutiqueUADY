// Panel de administración (versión Java): sesión, menú por rol, avisos y utilidades de fetch.
// Cada página declara en <body data-rol="principal|sucursal|cualquiera"> quién puede verla.
const Admin = (() => {
    const API = '../api/admin/';
    let sesion = null;

    const h = (t) => String(t ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    const dinero = (n) => '$' + Number(n || 0).toFixed(2);
    const cap = (t) => t ? t.charAt(0).toUpperCase() + t.slice(1) : '';
    const ESTADOS = ['pendiente', 'listo para recoger', 'entregado', 'cancelado'];
    const TALLAS = ['XS', 'S', 'M', 'L', 'XL', '2XL'];

    const get = async (ruta) => {
        const r = await fetch(API + ruta, { cache: 'no-store' });
        const datos = await r.json().catch(() => ({}));
        if (r.status === 401) { location.href = 'login.html'; throw new Error('sin sesión'); }
        if (!r.ok) { throw new Error(datos.error || ('Error ' + r.status)); }
        return datos;
    };

    // POST JSON (o FormData) y aviso con alert() del resultado. Devuelve true si salió bien.
    const post = async (ruta, cuerpo, confirmar) => {
        if (confirmar && !confirm(confirmar)) { return false; }
        const esForm = cuerpo instanceof FormData;
        const r = await fetch(API + ruta, {
            method: 'POST',
            headers: esForm ? {} : { 'Content-Type': 'application/json' },
            body: esForm ? cuerpo : JSON.stringify(cuerpo)
        });
        const datos = await r.json().catch(() => ({}));
        if (r.status === 401) { location.href = 'login.html'; return false; }
        if (!r.ok || datos.ok === false) { alert(datos.error || datos.mensaje || 'No se pudo completar la acción.'); return false; }
        if (datos.mensaje) { alert(datos.mensaje); }
        return true;
    };

    const menuDe = (rol) => rol === 'principal'
        ? { 'index.html': 'Inicio', 'sucursales.html': 'Sucursales', 'administradores.html': 'Administradores' }
        : { 'index.html': 'Inicio', 'productos.html': 'Productos', 'inventario.html': 'Inventario', 'pedidos.html': 'Pedidos' };

    const pintarBarra = () => {
        const nav = document.querySelector('.barra nav');
        if (!nav) { return; }
        const botonTema = nav.querySelector('.boton-tema');
        nav.innerHTML = '';
        const actual = location.pathname.split('/').pop() || 'index.html';
        Object.entries(menuDe(sesion.rol)).forEach(([url, texto]) => {
            const a = document.createElement('a');
            a.href = url; a.textContent = texto;
            if (url === actual) { a.className = 'activo'; }
            nav.appendChild(a);
        });
        const saludo = document.createElement('span');
        saludo.className = 'saludo';
        saludo.textContent = sesion.nombre + ' · ' + (sesion.rol === 'principal' ? 'Administrador principal' : 'Admin. de sucursal ' + sesion.sucursal);
        nav.appendChild(saludo);
        const salir = document.createElement('a');
        salir.href = '#'; salir.className = 'enlace-salir'; salir.textContent = 'Salir';
        salir.addEventListener('click', async (e) => {
            e.preventDefault();
            if (!confirm('¿Salir del panel de administración?')) { return; }
            await fetch(API + 'sesion', { method: 'DELETE' });
            location.href = '../index.html';
        });
        nav.appendChild(salir);
        if (botonTema) { nav.appendChild(botonTema); }
    };

    // Comprueba la sesión y el rol permitido por la página; devuelve la sesión o redirige
    const iniciar = async () => {
        const r = await fetch(API + 'sesion', { cache: 'no-store' });
        sesion = await r.json();
        if (!sesion.autenticado) { location.replace('login.html'); return null; }
        const rolPagina = document.body.dataset.rol || 'cualquiera';
        if (rolPagina !== 'cualquiera' && rolPagina !== sesion.rol) {
            alert('No tienes permiso para esa sección.');
            location.replace('index.html');
            return null;
        }
        pintarBarra();
        return sesion;
    };

    // Validación de formularios con alert (campos required vacíos, correo, contraseña corta)
    const validar = (form) => {
        for (const campo of form.querySelectorAll('[required]')) {
            if (String(campo.value).trim() === '') {
                alert('Por favor, llena el campo "' + (campo.closest('label')?.firstChild?.textContent.trim() || campo.name) + '".');
                campo.focus();
                return false;
            }
        }
        const correo = form.elements['correo'];
        if (correo && correo.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo.value)) { alert('El correo no es válido.'); correo.focus(); return false; }
        const pass = form.elements['password'];
        if (pass && pass.value.length > 0 && pass.value.length < 6) { alert('La contraseña debe tener al menos 6 caracteres.'); pass.focus(); return false; }
        return true;
    };

    const el = (etiqueta, clase, texto) => {
        const e = document.createElement(etiqueta);
        if (clase) { e.className = clase; }
        if (texto !== undefined) { e.textContent = texto; }
        return e;
    };

    return { get, post, iniciar, validar, h, dinero, cap, el, ESTADOS, TALLAS, sesion: () => sesion };
})();
