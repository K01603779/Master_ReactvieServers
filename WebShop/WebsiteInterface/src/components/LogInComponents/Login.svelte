<script>
	import {login,showRegister} from "../../stores/store.js";
	let username = "m.h@jku.at";
	let password = "12345";
	function mylogin(){
		login(username,password)
	}
	function register(){
		showRegister();
	}
</script>

<style>
	button{
		display: inline-block;
		padding: 10px 20px;
		font-size: 12px;
		cursor: pointer;
		text-align: center;
		text-decoration: none;
		outline: none;
		color: #fff;
		background-color: #4e7af3;
		border: none;
		border-radius: 10px;
		box-shadow: 0 9px #999;
	}
	button:hover {
		background-color: #4a95f7;
	}

	button:active {
		background-color: #3e6dee;
		box-shadow: 0 5px #666;
		transform: translateY(4px);
	}
	input[type="email"] {
		width: 15%;
		padding: 10px 15px;
		margin: 6px 0;
		box-sizing: content-box;
        border: 2px solid black;
        border-radius: 4px;
		background-color: white;
		color: black;
    }
    input[type="password"] {
		width: 15%;
		padding: 10px 15px;
		margin: 6px 0;
		box-sizing: content-box;
        border: 2px solid black;
        border-radius: 4px;
		background-color: white;
		color: black;
	}
    input:focus {
		background-color: lightblue;
	}
</style>

<div>
	<h2>Not Logged In </h2>
	Login : 
	<input bind:value={username} type="email" />
	Password :
	<input bind:value={password} type="password" />
	<button on:click={mylogin}>Login</button>
	<button on:click={register}>Register</button>
</div>
