<script>
    import {removeItem,addItem} from "../../../src/stores/store";
    export let amount;
    export let item;
    let removedamount =1;

    function remove(){
        if(removedamount >0 && removedamount<= amount){
            removeItem(item.itemID,removedamount);
        }
    }
    function add(){
        if(removedamount >0){
            addItem(item.itemID,removedamount);
        }
    }
</script>
<style>
    button{
		display: inline-block;
		padding: 9px 18px;
		font-size: 12px;
		cursor: pointer;
		text-align: center;
		text-decoration: none;
		outline: none;
		color: #fff;
		background-color: #f34e4e;
		border: none;
		border-radius: 8px;
	}
    .remove{		
		background-color: #f34e4e;
	}
	button:hover {
		background-color: #4a95f7;
	}
    .add{
		background-color: #67da93;
	}

	button:active {
		background-color: #3e6dee;
		box-shadow: 0 5px #666;
		transform: translateY(4px);
	}
</style>
<div>
    Amount :{amount} Name : {item.itemName} Description :{item.itemDescription} Price: {item.price}
    <input type="number" bind:value={removedamount} min="1" max={amount} />
    <button class="remove" on:click={remove}> Remove From Cart</button>
    <button class="add" on:click={add}> Add to  Cart</button>
</div>
