<?php
// Conexión a la base de datos y manejo de sesiones
require 'conexion.php';
session_start();

$mensaje = "";

// Si el usuario ya está logueado, lo mandamos al catálogo
if (isset($_SESSION['usuario_id'])) {
    header("Location: catalogo.html");
    exit();
}

// Procesar el formulario cuando se envía
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Recibir y limpiar los datos
    $nombre = trim($_POST['nombre']);
    $apellidos = trim($_POST['apellidos']);
    $correo = trim($_POST['correo']);
    $telefono = trim($_POST['telefono']);
    $password = $_POST['password'];
    $password2 = $_POST['password2'];

    // Validar que no estén vacíos
    if (empty($nombre) || empty($apellidos) || empty($correo) || empty($password)) {
        $mensaje = "<div class='error'>Por favor, llena todos los campos obligatorios.</div>";
    } elseif ($password !== $password2) {
        $mensaje = "<div class='error'>Las contraseñas no coinciden.</div>";
    } else {
        // Encriptar la contraseña antes de guardarla (por seguridad)
        $password_hash = password_hash($password, PASSWORD_DEFAULT);

        // Verificar si el correo ya existe en la base de datos
        $stmt = $conexion->prepare("SELECT id_usuario FROM usuarios WHERE correo = ?");
        $stmt->bind_param("s", $correo);
        $stmt->execute();
        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            $mensaje = "<div class='error'>Este correo ya está registrado. Intenta con otro.</div>";
        } else {
            // Insertar el nuevo usuario
            $stmt_insert = $conexion->prepare("INSERT INTO usuarios (nombre, apellidos, correo, password_hash, telefono) VALUES (?, ?, ?, ?, ?)");
            $stmt_insert->bind_param("sssss", $nombre, $apellidos, $correo, $password_hash, $telefono);
            
            if ($stmt_insert->execute()) {
                $mensaje = "<div class='ok'>¡Cuenta creada con éxito! Ya puedes <a href='login.php'>iniciar sesión</a>.</div>";
            } else {
                $mensaje = "<div class='error'>Error al crear la cuenta. Intenta de nuevo.</div>";
            }
            $stmt_insert->close();
        }
        $stmt->close();
    }
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Crear cuenta | Boutique UADY</title>
  <link rel="stylesheet" href="assets/css/estilo.css">
</head>
<body>
<header class="barra">
  <!-- Ahora el inicio es index.php -->
  <a class="marca" href="index.php">Boutique <b>UADY</b></a>
  <nav>
    <a href="catalogo.html">Catálogo</a>
    <a href="login.php">Iniciar sesión</a>
    <a class="boton" href="registro.php">Crear cuenta</a>
  </nav>
</header>
<main>
<section class="formulario">
  <h1>Crear cuenta</h1>
  
  <!-- Aquí se muestran los mensajes de error o éxito -->
  <?php echo $mensaje; ?>

  <!-- El form ya apunta a registro.php y no tiene el novalidate para que el navegador apoye -->
  <form action="registro.php" method="post">
    <label>Nombre <input name="nombre" required></label>
    <label>Apellidos <input name="apellidos" required></label>
    <label>Correo <input type="email" name="correo" required></label>
    <label>Teléfono (opcional) <input name="telefono"></label>
    <label>Contraseña <input type="password" name="password" required></label>
    <label>Repite la contraseña <input type="password" name="password2" required></label>
    <button class="boton grande" type="submit">Crear cuenta</button>
  </form>
  <p class="aparte">¿Ya tienes cuenta? <a href="login.php">Inicia sesión</a></p>
</section>
</main>
<footer class="pie">
  Boutique UADY en línea · Proyecto integrador de Desarrollo de Aplicaciones Web · Facultad de Matemáticas
</footer>
</body>
</html>