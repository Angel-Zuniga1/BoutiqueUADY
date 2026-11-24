# Boutique UADY en línea

Sistema web para la venta de productos institucionales de las boutiques de la UADY.
El cliente consulta el catálogo, revisa existencias por sucursal y talla, arma su carrito y
aparta su pedido para recogerlo y pagarlo en la sucursal que elija. Cada sucursal administra
sus productos, inventario y pedidos, y un administrador principal supervisa todas las sucursales.

Proyecto final de **Construcción de Software** · Facultad de Matemáticas, UADY.

## Estructura

```
BoutiqueUADY/
├── DataBase/          Script de la base de datos (boutique_uady.sql)
├── Docs/              Documentación: diagramas y guía de refactorización
├── src/
│   ├── main/java/     Backend en Java (servlets, servicios, DAO, modelos)
│   ├── main/webapp/   Frontend (HTML, CSS, JavaScript e imágenes)
│   └── test/java/     Pruebas unitarias (JUnit 5)
└── pom.xml            Configuración de Maven
```

## Tecnologías

Java 17 · Jakarta Servlet 6 · Apache Tomcat 10.1 · MySQL (XAMPP) · Maven · JUnit 5 · NetBeans

## Cómo ejecutarlo

1. En XAMPP enciende **MySQL** e importa `DataBase/boutique_uady.sql` en phpMyAdmin.
2. Abre la carpeta del proyecto en NetBeans (**File → Open Project**).
3. En **Properties → Run** elige **Apache Tomcat 10.1**.
4. **Clean and Build** y luego **Run**.
5. Abre `http://localhost:8080/BoutiqueUady/`

Las pruebas se ejecutan con **Test** (Alt+F6) en NetBeans o con `mvn test`.

## Cuentas de prueba

| Para qué | Correo | Contraseña |
|---|---|---|
| Paso 1: acceso al panel (en el login de la tienda) | `admin@uady.com` | `12345678` |
| Paso 2: administrador principal | `A24216404@alumnos.uady.mx` | `3431halo` |
| Administrador de sucursal | la que cree el principal | |
| Cliente | regístrate desde la tienda | |

## Documentación

- `Docs/REFACTORIZACION.md`: code smells identificados y técnica de refactorización sugerida para cada uno.
- `Docs/Diagramas/`: diagramas del sistema.
