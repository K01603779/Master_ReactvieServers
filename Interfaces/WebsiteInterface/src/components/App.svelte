<script>
	import Tab from "../tabs/tab.svelte";
	import Tablist from "../tabs/tablist.svelte";
	import Tabpanel from "../tabs/tabpanel.svelte";
	import Tabs from "../tabs/Tabs.svelte";
	import Cart from "./Cart/cart.svelte";
	import Header from "./Header/Header.svelte";
	import Login from "./LogInComponents/Login.svelte";
	import Register from "./LogInComponents/Register.svelte";
	import Search from "./Search/search.svelte";
	import Transaction from "./Transactions/transaction.svelte";
	import {register} from "../stores/store.js";
</script>

<style>
</style>

<div>
	<Tabs>
		<Tablist>
			<Tab>Login</Tab>
			<Tab>Search Items</Tab>
			<Tab>Shopping Cart</Tab>
			<Tab>View Transactions</Tab>			
		</Tablist>
		<Tabpanel>
			<Header />
			{#if $register}
				<Register />
			{/if}
		</Tabpanel>
		<Tabpanel>
			<Search />
		</Tabpanel>
		<Tabpanel>
			<Cart />
		</Tabpanel>
		<Tabpanel>
			<Transaction />
		</Tabpanel>
		
	</Tabs>
</div>
