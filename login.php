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
    $correo = trim($_POST['correo']);
    $password = $_POST['password'];

    if (empty($correo) || empty($password)) {
        $mensaje = "<div class='error'>Por favor, ingresa tu correo y contraseña.</div>";
    } else {
        // Buscar al usuario en la base de datos por su correo
        $stmt = $conexion->prepare("SELECT id_usuario, nombre, password_hash FROM usuarios WHERE correo = ?");
        $stmt->bind_param("s", $correo);
        $stmt->execute();
        $resultado = $stmt->get_result();

        if ($resultado->num_rows === 1) {
            $usuario = $resultado->fetch_assoc();
            
            // Verificar si la contraseña ingresada coincide con la encriptada en la BD
            if (password_verify($password, $usuario['password_hash'])) {
                // ¡Contraseña correcta! Creamos la sesión
                $_SESSION['usuario_id'] = $usuario['id_usuario'];
                $_SESSION['usuario_nombre'] = $usuario['nombre'];
                
                // Redirigir al catálogo
                header("Location: catalogo.html");
                exit();
            } else {
                $mensaje = "<div class='error'>Contraseña incorrecta.</div>";
            }
        } else {
            $mensaje = "<div class='error'>No existe una cuenta con ese correo.</div>";
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
  <title>Iniciar sesión | Boutique UADY</title>
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
  <h1>Iniciar sesión</h1>
  
  <!-- Aquí se muestran los mensajes de error -->
  <?php echo $mensaje; ?>

  <form action="login.php" method="post">
    <label>Correo <input type="email" name="correo" required></label>
    <label>Contraseña <input type="password" name="password" required></label>
    <button class="boton grande" type="submit">Entrar</button>
  </form>
  <p class="aparte">¿Aún no tienes cuenta? <a href="registro.php">Créala aquí</a></p>
</section>
</main>
<footer class="pie">
  Boutique UADY en línea · Proyecto integrador de Desarrollo de Aplicaciones Web · Facultad de Matemáticas
</footer>
</body>
</html>