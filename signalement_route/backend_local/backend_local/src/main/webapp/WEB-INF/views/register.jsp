<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Inscription</title>
</head>
<body>

<h2>Inscription</h2>

<form id="registerForm">
    <input type="text" placeholder="Nom" id="nom"><br><br>
    <input type="email" placeholder="Email" id="email"><br><br>
    <input type="password" placeholder="Mot de passe" id="password"><br><br>
    <button type="button" onclick="register()">S'inscrire</button>
</form>

<script>
function register() {
    fetch("http://localhost:8081/auth/register", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            nom: document.getElementById("nom").value,
            email: document.getElementById("email").value,
            password: document.getElementById("password").value
        })
    })
    .then(res => res.json())
    .then(data => console.log(data));
}
</script>

</body>
</html>
