<script>
import Item from "../Search/Item.svelte";

    export let transactionID;
    export let entries;
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
	.rounded-list li{
		margin: 0 0 0 3em; /* Add some left margin for inner lists */
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
<div class="rounded-list">
{#each entries as entry}
	<!--<li>Name: {entry.item.name} Description: {entry.item.description} Amount: {entry.amount}  Price: {entry.item.price} </li> !-->
	<li> {entry.item.itemName} Description {entry.item.itemDescription} Amount : {entry.amount}</li>
{/each}
</div>