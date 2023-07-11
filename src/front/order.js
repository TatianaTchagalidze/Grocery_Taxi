// Fetch product data and populate the product container
fetch('http://localhost:8080/products')
  .then(response => response.json())
  .then(data => {
    const productContainer = document.getElementById('product-container');
    data.forEach(product => {
      const productElement = createProductElement(product);
      productContainer.appendChild(productElement);
    });
  })
  .catch(error => {
    console.error('Error fetching product data:', error);
  });

function createProductElement(product) {
  const productElement = document.createElement('div');
  productElement.classList.add('product');

  const imageElement = document.createElement('img');
  imageElement.src = `images/${product.id}.png`; // Set the product image URL
  imageElement.alt = product.name; // Set the product name as the alt text
  productElement.appendChild(imageElement);

  const nameElement = document.createElement('h3');
  nameElement.textContent = product.name;
  productElement.appendChild(nameElement);

  const priceElement = document.createElement('p');
  priceElement.textContent = `Price: $${product.price}`;
  productElement.appendChild(priceElement);

  const descriptionElement = document.createElement('p');
  descriptionElement.textContent = product.description;
  productElement.appendChild(descriptionElement);

  const quantityElement = document.createElement('input');
  quantityElement.type = 'number';
  quantityElement.min = 1;
  quantityElement.value = 1;
  productElement.appendChild(quantityElement);

  const addToCartButton = document.createElement('button');
  addToCartButton.textContent = 'Add to Cart';
  addToCartButton.addEventListener('click', () => {
    const quantity = parseInt(quantityElement.value, 10);
    addToCart(product, quantity);
  });
  productElement.appendChild(addToCartButton);

  return productElement;
}

// Search functionality
const searchInput = document.getElementById('search-input');
const searchButton = document.getElementById('search-button');
searchButton.addEventListener('click', searchProducts);

function searchProducts() {
  const searchQuery = searchInput.value.toLowerCase();
  const productElements = document.getElementsByClassName('product');

  // Iterate through each product element and check if it matches the search query
  for (const productElement of productElements) {
    const productName = productElement.querySelector('h3').textContent.toLowerCase();
    const productDescription = productElement.querySelector('p').textContent.toLowerCase();

    if (productName.includes(searchQuery) || productDescription.includes(searchQuery)) {
      productElement.style.display = 'block';
    } else {
      productElement.style.display = 'none';
    }
  }
}


// Cart functionality
const cartItems = document.getElementById('cart-items');
const checkoutButton = document.getElementById('checkout-button');
checkoutButton.addEventListener('click', checkoutCart);

const cart = [];

function addToCart(product, quantity) {
  const cartItem = {
    product: product,
    quantity: quantity
  };
  cart.push(cartItem);
  updateCartDisplay();
}

function updateCartDisplay() {
  cartItems.innerHTML = '';

  cart.forEach(cartItem => {
    const cartItemElement = document.createElement('li');
    cartItemElement.textContent = `${cartItem.product.name} - Quantity: ${cartItem.quantity}`;
    cartItems.appendChild(cartItemElement);
  });
}


function checkoutCart() {
}


