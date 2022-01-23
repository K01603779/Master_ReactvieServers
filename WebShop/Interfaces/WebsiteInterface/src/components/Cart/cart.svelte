<script>
	import Cartentry from "./cartentry.svelte";
	import { cart, checkOut,loggedIn } from "../../../src/stores/store";
	function checkout(){
		if ($loggedIn ==true){
			checkOut();
		}else{
			alert("not logged in");
		}
	}
</script>

<style>
	.rounded-list {
		counter-reset: li; /* Initiate a counter */
		list-style: none; /* Remove default numbering */
		*list-style: decimal; /* Keep using default numbering for IE6/7 */
		font: 15px "trebuchet MS", "lucida sans";
		padding: 0;
		margin-bottom: 4em;
		text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
	}
	.rounded-list li {
		position: relative;
		display: block;
		padding: 0.4em 0.4em 0.4em 2em;
		*padding: 0.4em;
		margin: 0.5em 0;
		background: #ddd;
		color: #444;
		text-decoration: none;
		border-radius: 0.3em;
		transition: all 0.3s ease-out;
	}

	.rounded-list li:hover {
		background: #eee;
	}

	.rounded-list li:hover:before {
		transform: rotate(360deg);
	}

	.rounded-list li:before {
		content: counter(li);
		counter-increment: li;
		position: absolute;
		left: -1.3em;
		top: 50%;
		margin-top: -1.3em;
		background: #87ceeb;
		height: 2em;
		width: 2em;
		line-height: 2em;
		border: 0.3em solid #fff;
		text-align: center;
		font-weight: bold;
		border-radius: 2em;
		transition: all 0.3s ease-out;
	}
	button{
		display: inline-block;
		padding: 10px 20px;
		font-size: 12px;
		cursor: pointer;
		text-align: center;
		text-decoration: none;
		outline: none;
		color: #fff;
		background-color: #42f592;
		border: none;
		border-radius: 10px;
		box-shadow: 0 9px #999;
	}
	button:hover {
		background-color: #4af767;
	}

	button:active {
		background-color: #35c44d;
		box-shadow: 0 5px #666;
		transform: translateY(4px);
	}
</style>

<h2>Shopping Cart</h2>

<div class="rounded-list">
	{#if $cart.length !=0}
		{#each $cart as entry}
			<li>
				<Cartentry amount={entry.amount} item={entry.item} />
			</li>
		{/each}
		<button on:click="{checkout}"> CheckOut</button>
	{/if}
</div>
