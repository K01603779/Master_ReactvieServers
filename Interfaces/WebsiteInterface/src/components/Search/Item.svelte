<script>
	import { addItem } from "../../../src/stores/store";
	export let itemid;
	export let itemname;
	export let itemdescription;
	export let price;
	let ammount = 1;
	function addtoCart() {
		if (ammount >= 1) {
			addItem(itemid, ammount);
		} else {
			console.log("amount <1 amount: " + amount);
		}
	}
</script>

<style>
	button {
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
	}

	button {
		background-color: #4a95f7;
	}
</style>

<div>
	<label for="itemid">{itemid}<label> ,
			{itemname} ,
			{itemdescription} ,
			{price} ,
			<input type="number" bind:value={ammount} min="1" />
			<button on:click={addtoCart}> Add To Cart</button>
		</label></label>
</div>
