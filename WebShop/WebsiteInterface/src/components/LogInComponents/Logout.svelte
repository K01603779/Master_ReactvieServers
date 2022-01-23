<script>
	import { logout,user } from "../../stores/store.js";
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
</style>
<h2>Logged In as {$user}</h2>
<div><button on:click={logout}>Logout</button></div>
