<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Connexion</title>
</head>
<body>

<h2>Connexion</h2>

<form id="loginForm">
    <input type="email" placeholder="Email" id="email"><br><br>
    <input type="password" placeholder="Mot de passe" id="password"><br><br>
    <button type="button" onclick="login()">Se connecter</button>
</form>

<script>
function login() {
    fetch("http://localhost:8081/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            email: document.getElementById("email").value,
            password: document.getElementById("password").value
        })
    })
    .then(res => {
        if (!res.ok) alert("Échec de connexion");
        else return res.json();
    })
    .then(data => console.log(data));
}
</script>

</body>
</html>
