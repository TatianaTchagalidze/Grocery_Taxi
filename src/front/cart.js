const cartItemsFromStorage = JSON.parse(sessionStorage.getItem('cart'));

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

      listItem.appendChild(productContainer);
      cartItemsList.appendChild(listItem);

      totalPrice += totalPriceForItem;
    });

    totalPriceElement.textContent = `Total Price: $${totalPrice.toFixed(2)}`;
    cartItemsList.appendChild(totalPriceElement);

    const confirmOrderButton = document.createElement('button');
    confirmOrderButton.textContent = 'Confirm Order';
    confirmOrderButton.addEventListener('click', handleConfirmOrder);
    cartItemsList.appendChild(confirmOrderButton);

    const modifyButton = document.createElement('button');
    modifyButton.textContent = 'Modify Order';
    modifyButton.addEventListener('click', handleModifyOrder);
    cartItemsList.appendChild(modifyButton);
  } else {
    const emptyText = document.createElement('p');
    emptyText.textContent = 'Your cart is empty.';
    cartItemsList.appendChild(emptyText);
  }
}

// Function to update the quantity of a cart item
function updateCartItemQuantity(item, quantity) {
  const orderId = item.orderId;
  const itemId = item.itemId;
  const requestBody = {
    quantity: quantity
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
          return response.json();
        } else {
          throw new Error('Failed to update item quantity.');
        }
      })
      .then(data => {
        // Item quantity updated successfully
        item.quantity = quantity;
        updateCartDisplay();
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Failed to update item quantity. Please try again.');
      });
}

// Function to update the cart display
function updateCartDisplay() {
  renderCartItems(cartItemsFromStorage);
}

// Function to handle continuing to pay
function handleConfirmOrder() {
  alert('You will be redirected to track your order.');
}

function handleModifyOrder() {
  // Redirect to orders.html
  window.location.href = 'orders.html';
}
