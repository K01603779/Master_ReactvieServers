<script>
	import { registerUser } from "../../stores/store";
	let email = "";
	let password = "";
	let firstname = "";
	let lastname = "";
	let address = "";
	let creditCard="";
	function register() {
		if (
			email != "" &&
			password != "" &&
			firstname != "" &&
			lastname != "" &&
			address != "" &&
			creditCard != ""
		) {
			registerUser(email, password, firstname, lastname, address,creditCard);
		}
		else{
			alert(email + " " +password +" " + lastname + " " + address + " " +firstname);
		}
	}
</script>

<style>
	* {
		box-sizing: border-box;
	}

	input[type="text"],
	input[type="email"],
	input[type="password"] {
		width: 100%;
		padding: 12px;
		border: 1px solid #ccc;
		border-radius: 4px;
		resize: vertical;
	}
	label {
		padding: 12px 12px 12px 0;
		display: inline-block;
	}

	input[type="button"] {
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
		float: right;
	}

	input[type="button"]:hover {
		background-color: #4a95f7;
	}

	.container {
		border-radius: 5px;
		background-color: #f2f2f2;
		padding: 20px;
	}

	.col-25 {
		float: left;
		width: 25%;
		margin-top: 6px;
	}

	.col-75 {
		float: left;
		width: 75%;
		margin-top: 6px;
	}

	/* Clear floats after the columns */
	.row:after {
		content: "";
		display: table;
		clear: both;
	}

	/* Responsive layout - when the screen is less than 600px wide, make the two columns stack on top of each other instead of next to each other */
	@media screen and (max-width: 600px) {
		.col-25,
		.col-75,
		input[type="button"] {
			width: 100%;
			margin-top: 0;
		}
	}
</style>

<div>
	<h1>Register new User</h1>
	<div class="container">
		<div class="row">
			<div class="col-25"><label for="fname">Firstname</label></div>
			<div class="col-75">
				<input
					type="text"
					id="fname"
					name="fname"
					placeholder="Your firstname"
					bind:value={firstname} />
			</div>
		</div>
		<div class="row">
			<div class="col-25"><label for="lname">Lastname</label></div>
			<div class="col-75">
				<input
					type="text"
					id="lname"
					name="lname"
					placeholder="Your lastname"
					bind:value={lastname} />
			</div>
		</div>
		<div class="row">
			<div class="col-25"><label for="email">Email</label></div>
			<div class="col-75">
				<input
					type="email"
					id="email"
					name="email"
					placeholder="Your email address"
					bind:value={email} />
			</div>
		</div>
		<div class="row">
			<div class="col-25"><label for="password">Password</label></div>
			<div class="col-75">
				<input
					type="password"
					id="password"
					name="password"
					placeholder="Your password"
					bind:value={password} />
			</div>
		</div>
		<div class="row">
			<div class="col-25"><label for="address">Address</label></div>
			<div class="col-75">
				<input
					type="text"
					id="address"
					name="address"
					placeholder="Your address"
					bind:value={address} />
			</div>
		</div>
		<div class="row">
			<div class="col-25"><label for="Credit card">CreditCard</label></div>
			<div class="col-75">
				<input
					type="text"
					id="creditCard"
					name="creditCard"
					placeholder="Your credit card"
					bind:value={creditCard} />
			</div>
		</div>
		<div class="row">
			<input type="button" value="Sumbit" on:click={register} />
		</div>
	</div>
</div>
