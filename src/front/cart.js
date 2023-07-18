// Constants
const cartItemsFromStorage = JSON.parse(sessionStorage.getItem('cart'));
const orderFromStorage = JSON.parse(sessionStorage.getItem('order'));
const orderItems = orderFromStorage.orderItems;
const itemIds = orderItems.map(orderItem => orderItem.id);


// Retrieve the order object from storage

const cartItemsList = document.getElementById('cart-items');
const totalPriceElement = document.createElement('p');
totalPriceElement.classList.add('total-price');


renderCartItems(cartItemsFromStorage);

function renderCartItems(cartItems) {
  cartItemsList.innerHTML = '';

  if (cartItems && cartItems.length > 0) {
    const orderText = document.createElement('p');
    orderText.textContent = 'This is your order:';
    cartItemsList.appendChild(orderText);

    let totalPrice = 0;

    cartItems.forEach(item => {
      const listItem = document.createElement('li');

      // Create a container for the product information
      const productContainer = document.createElement('div');
      productContainer.classList.add('cart-item');

      // Create an image element for the product
      const imageElement = document.createElement('img');
      imageElement.src = `images/${item.product.id}.png`; // Set the product image URL
      imageElement.alt = item.product.name; // Set the product name as the alt text
      imageElement.classList.add('cart-item-image');
      productContainer.appendChild(imageElement);

      // Create a span element for displaying the product name
      const nameElement = document.createElement('span');
      nameElement.textContent = item.product.name;
      nameElement.classList.add('cart-item-name');
      productContainer.appendChild(nameElement);

      // Create an input element for changing the quantity
      const quantityInput = document.createElement('input');
      quantityInput.type = 'number';
      quantityInput.min = 1;
      quantityInput.value = item.quantity;
      quantityInput.classList.add('cart-item-quantity');
      quantityInput.addEventListener('change', () => {
        const quantity = parseInt(quantityInput.value, 10);
        updateCartItemQuantity(item, quantity);
      });
      productContainer.appendChild(quantityInput);

      // Create a span element for displaying the product price
      const priceElement = document.createElement('span');
      const totalPriceForItem = item.quantity * item.product.price;
      priceElement.textContent = `$${totalPriceForItem.toFixed(2)}`;
      priceElement.classList.add('cart-item-price');
      productContainer.appendChild(priceElement);

      const removeButton = document.createElement('button');
      removeButton.innerHTML = '&#10006;'; // Add the "X" symbol as innerHTML
      removeButton.classList.add('btn', 'btn-danger');
      removeButton.addEventListener('click', () => {
        removeCartItem(item);
      });
      productContainer.appendChild(removeButton);

      listItem.appendChild(productContainer);
      cartItemsList.appendChild(listItem);

      totalPrice += totalPriceForItem;
    });

    totalPriceElement.textContent = `Total Price: $${totalPrice.toFixed(2)}`;
    cartItemsList.appendChild(totalPriceElement);


    const confirmOrderButton = document.createElement('button');
    confirmOrderButton.textContent = 'Confirm Order';
    confirmOrderButton.classList.add('btn', 'btn-primary');
    confirmOrderButton.addEventListener('click', handleConfirmOrder);
    cartItemsList.appendChild(confirmOrderButton);

    const modifyButton = document.createElement('button');
    modifyButton.textContent = 'Modify Order';
    modifyButton.classList.add('btn', 'btn-secondary');
    modifyButton.addEventListener('click', handleModifyOrder);
    cartItemsList.appendChild(modifyButton);

  }
}

function updateCartItemQuantity(item, quantity) {
  const productName = item.product.name; // Retrieve the productName from the item object

  const orderItems = JSON.parse(sessionStorage.getItem('order')).orderItems;
  const orderItem = orderItems.find(orderItem => orderItem.productName === productName);

  const itemId = orderItem.id;
  const orderId = JSON.parse(sessionStorage.getItem('order')).id;

  const requestBody = {
    quantity: quantity,
  };

  fetch(`http://localhost:8080/orders/${orderId}/items/${itemId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestBody),
    credentials: 'include'
  })
      .then(response => {
        if (response.ok) {
          return response;
        } else {
          throw new Error('Failed to update item quantity.');
        }
      })
      .then(data => {
        // Item quantity updated successfully in the database
        item.quantity = quantity; // Update the quantity in the cartItemsFromStorage
        updateCartDisplay(); // Update the cart display with the new quantity
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Failed to update item quantity. Please try again.');
      });
}


function removeCartItem(item) {
  const productName = item.product.name; // Retrieve the productName from the item object

  const orderItems = JSON.parse(sessionStorage.getItem('order')).orderItems;
  const orderItem = orderItems.find(orderItem => orderItem.productName === productName);

  if (!orderItem) {
    console.error('Error: Order item not found.');
    return;
  }

  const itemId = orderItem.id;
  const orderId = JSON.parse(sessionStorage.getItem('order')).id;

  fetch(`http://localhost:8080/orders/${orderId}/items/${itemId}`, {
    method: 'DELETE',
    credentials: 'include'
  })
      .then(response => {
        if (response.ok) {
          // Item removed successfully from the database, now update the cart display
          const updatedCartItems = JSON.parse(sessionStorage.getItem('cart')).filter(
              cartItem => cartItem.product.name !== productName
          );

          // Update the cart in session storage
          sessionStorage.setItem('cart', JSON.stringify(updatedCartItems));

          // Update the cart display
          updateCartDisplay();
          return response;
        } else {
          throw new Error('Failed to remove item from cart.');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Failed to remove item from cart. Please try again.');
      });
}





// Function to update the cart display
function updateCartDisplay() {
  const cartItemsFromStorage = JSON.parse(sessionStorage.getItem('cart'));
  renderCartItems(cartItemsFromStorage);

  // Recalculate the total price
  let totalPrice = 0;
  cartItemsFromStorage.forEach(item => {
    const totalPriceForItem = item.quantity * item.product.price;
    totalPrice += totalPriceForItem;
  });
  totalPriceElement.textContent = `Total Price: $${totalPrice.toFixed(2)}`;
}


// Function to handle continuing to pay
function handleConfirmOrder() {
  const orderId = orderFromStorage.id; // Use the order ID from the stored order object

  fetch(`http://localhost:8080/orders/${orderId}/confirm`, {
    method: 'PUT',
    credentials: 'include'
  })
      .then(response => {
        if (response.ok) {
          alert('Order confirmed successfully.');
          window.location.href = 'tracking.html'; // Redirect to tracking.html
        } else {
          throw new Error('Failed to confirm order.');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Failed to confirm order. Please try again.');
      });
}

function handleModifyOrder() {
  // Redirect to orders.html
  window.location.href = 'orders.html';
}