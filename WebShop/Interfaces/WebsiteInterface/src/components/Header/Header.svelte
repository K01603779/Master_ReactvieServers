<script>
import Login from "../LogInComponents/Login.svelte"
import Logout from "../LogInComponents/Logout.svelte";
import {loggedIn} from "../../../src/stores/store";
</script>

<div>
	<h1>Simple Webshop</h1>
	{#if $loggedIn}
     <Logout></Logout>
	{:else}
	 <Login></Login>
	{/if}
</div>
