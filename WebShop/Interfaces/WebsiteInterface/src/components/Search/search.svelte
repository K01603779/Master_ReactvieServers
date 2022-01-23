<script>
	import Item from "./Item.svelte";
	import { searchresults, search } from "../../stores/store.js";
	let searchstr = "";
	function findItems() {
		search(searchstr);
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
	input[type="text"] {
		width: 75%;
		box-sizing: border-box;
		border: 2px solid #ccc;
		border-radius: 4px;
		font-size: 16px;
		background-color: white;
		background-image: url("searchicon.png");
		background-position: 10px 10px;
		background-repeat: no-repeat;
		padding: 10px 15px 10px 30px;
	}
	button:after {
		content: "";
		display: table;
		clear: both;
	}
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
</style>

<h2>Search Items</h2>
<div>
	<input type="text" bind:value={searchstr} placeholder="Search.." />
	<button on:click={findItems}> Search </button>
</div>
<div class="rounded-list">
	{#each $searchresults as item}
		<li>
			<Item
				itemid="{item.itemID}"
				itemname="{item.itemName}"
				itemdescription="{item.itemDescription}"
				price={item.price} />
		</li>
	{/each}
</div>
