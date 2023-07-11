const cartItemsFromStorage = JSON.parse(localStorage.getItem('cartItems'));

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

    const continueButton = document.createElement('button');
    continueButton.textContent = 'Continue to Pay';
    continueButton.addEventListener('click', continueToPay);
    cartItemsList.appendChild(continueButton);

    const modifyButton = document.createElement('button');
    modifyButton.textContent = 'Modify Order';
    modifyButton.addEventListener('click', modifyOrder);
    cartItemsList.appendChild(modifyButton);
  } else {
    const emptyText = document.createElement('p');
    emptyText.textContent = 'Your cart is empty.';
    cartItemsList.appendChild(emptyText);
  }
}

// Function to update the quantity of a cart item
function updateCartItemQuantity(item, quantity) {
  item.quantity = quantity;
  updateCartDisplay();
}

// Function to update the cart display
function updateCartDisplay() {
  renderCartItems(cart);
}

// Function to handle continuing to pay
function continueToPay() {
  alert('You will be redirected to the payment page.');
}

// Function to handle modifying the order
function modifyOrder() {
  alert('You will be redirected to the order modification page.');
}
