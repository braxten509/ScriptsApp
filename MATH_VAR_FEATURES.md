# Regex Editor - MATH and VAR Features

## Overview

The regex editor now supports mathematical expressions and variable storage outside of conditional statements. This allows for advanced calculations and data storage throughout template processing.

## New Syntax

### {MATH expression} - Standalone Mathematical Expressions

Use `{MATH expression}` blocks to perform calculations and display the results directly in your template output.

**Syntax:**
```
{MATH mathematical_expression}
```

**Examples:**
```
Basic arithmetic:
{MATH 5 + 3}        → 8
{MATH 10 * 4}       → 40
{MATH 100 / 5}      → 20
{MATH 15 - 7}       → 8
{MATH 17 % 5}       → 2

With functions:
{MATH sqrt(16)}     → 4
{MATH pow(2, 3)}    → 8
{MATH abs(-15)}     → 15
{MATH min(5, 10)}   → 5
{MATH max(3, 8)}    → 8

Complex expressions:
{MATH 3.14159 * pow(5, 2)}  → 78.53975
{MATH sqrt(pow(3, 2) + pow(4, 2))}  → 5
```

### {VAR name = expression} - Variable Declaration and Storage

Use `{VAR name = expression}` blocks to declare variables and store calculated values for later use.

**Syntax:**
```
{VAR variable_name = mathematical_expression}
```

**Variable Usage:**
After declaring a variable, you can reference it in three ways:
1. In other variable declarations: `{VAR total = subtotal + tax}`
2. In MATH expressions: `{MATH total * 0.1}`
3. In template output: `{variable_name}`

**Examples:**
```
Variable declaration:
{VAR tax_rate = 0.08}
{VAR base_price = 100}

Variable usage in calculations:
{VAR tax_amount = base_price * tax_rate}
{VAR total = base_price + tax_amount}

Variable usage in output:
Base Price: ${base_price}
Tax ({tax_rate * 100}%): ${tax_amount}
Total: ${total}

Output:
Base Price: $100
Tax (8%): $8
Total: $108
```

## Integration with Pattern References

Both MATH and VAR blocks can reference regex pattern matches, making them powerful for processing captured data.

**Pattern Reference Syntax:**
- `pattern.group(n)` - nth capture group from first match
- `pattern[index].group(n)` - nth capture group from specific match
- Within loops: `pattern.group(n)` refers to current iteration

**Example with Pattern Processing:**
```
Input: "Product $25, Product $40, Product $15"
Pattern 'prices': \$(\d+)

Template:
{VAR tax_rate = 0.1}
{VAR discount_threshold = 30}

{for prices}
Item: ${prices.group(1)}
{VAR item_price = prices.group(1)}
{VAR tax = item_price * tax_rate}
{VAR total = item_price + tax}

{if item_price >= discount_threshold}
{VAR discount = item_price * 0.05}
After 5% discount: {MATH item_price - discount}
{/if}

Tax: ${tax}
Total with tax: ${total}
---
{/for}

Output:
Item: $25
Tax: $2.5
Total with tax: $27.5
---
Item: $40
After 5% discount: $38
Tax: $4
Total with tax: $44
---
Item: $15
Tax: $1.5
Total with tax: $16.5
---
```

## Variable Scope

Variables declared with `{VAR}` are:
- **Global**: Available throughout the entire template processing
- **Persistent**: Retain their values across loop iterations
- **Overwritable**: Can be redeclared with new values

**Example of Variable Persistence:**
```
{VAR running_total = 0}

{for prices}
{VAR current_price = prices.group(1)}
{VAR running_total = running_total + current_price}
Item {prices}: ${current_price} (Running total: ${running_total})
{/for}

Final total: ${running_total}
```

## Mathematical Functions Available

All mathematical functions that work in `{if}` conditions are also available in `{MATH}` and `{VAR}` expressions:

### Basic Arithmetic
- `+` Addition
- `-` Subtraction  
- `*` Multiplication
- `/` Division
- `%` Modulo

### Mathematical Functions
- `abs(x)` - Absolute value
- `sqrt(x)` - Square root
- `pow(x, y)` - x to the power of y
- `min(x, y)` - Minimum of two values
- `max(x, y)` - Maximum of two values

### Order of Operations
Standard mathematical precedence is followed:
1. Parentheses `()`
2. Functions `sqrt()`, `pow()`, etc.
3. Multiplication `*`, Division `/`, Modulo `%`
4. Addition `+`, Subtraction `-`

## Best Practices

### 1. Use Descriptive Variable Names
```
Good:
{VAR sales_tax_rate = 0.08}
{VAR shipping_cost = 5.99}

Avoid:
{VAR x = 0.08}
{VAR y = 5.99}
```

### 2. Declare Variables at the Top
```
{VAR tax_rate = 0.08}
{VAR discount_rate = 0.1}
{VAR shipping_threshold = 50}

{for orders}
  <!-- Use variables here -->
{/for}
```

### 3. Use MATH for Display, VAR for Storage
```
{VAR calculated_total = base + tax + shipping}
Final Total: {MATH calculated_total}  <!-- Display -->

<!-- Rather than mixing calculation with display -->
Final Total: {MATH base + tax + shipping}
```

### 4. Combine with Conditional Logic
```
{VAR order_total = item_price + shipping}

{if order_total > 100}
  Discount: {MATH order_total * 0.1}
  {VAR final_total = order_total * 0.9}
{/if}

Final amount: ${final_total}
```

## Troubleshooting

### Common Issues

1. **Variable not found**: Make sure to declare variables with `{VAR}` before using them
2. **Division by zero**: Check denominator values when using division
3. **Invalid expressions**: Ensure proper syntax in mathematical expressions
4. **Pattern references**: Verify pattern names match exactly and groups exist

### Debug Output

Enable debug output to see variable assignments and calculations:
1. Check the "Debug" checkbox in the regex editor
2. View console output for detailed processing information

Example debug output:
```
DEBUG VAR: Set variable tax_rate = 0.08
DEBUG MATH: Evaluated base_price * tax_rate = 8.0
DEBUG VAR: Set variable tax_amount = 8.0
```

## Examples by Use Case

### E-commerce Price Calculations
```
{VAR tax_rate = 0.08}
{VAR shipping_rate = 5.99}
{VAR free_shipping_threshold = 50}

{for products}
Product: {products.group(1)}
Price: ${products.group(2)}

{VAR item_price = products.group(2)}
{VAR tax = item_price * tax_rate}
{VAR shipping = item_price >= free_shipping_threshold ? 0 : shipping_rate}
{VAR total = item_price + tax + shipping}

Tax: ${tax}
Shipping: ${shipping}
Total: ${total}
{/for}
```

### Report Calculations
```
{VAR total_sales = 0}
{VAR transaction_count = 0}

{for transactions}
{VAR amount = transactions.group(1)}
{VAR total_sales = total_sales + amount}
{VAR transaction_count = transaction_count + 1}
Transaction: ${amount}
{/for}

Summary:
Total Sales: ${total_sales}
Transaction Count: {transaction_count}
Average: {MATH total_sales / transaction_count}
```

### Unit Conversions
```
{VAR meters_to_feet = 3.28084}
{VAR kg_to_pounds = 2.20462}

{for measurements}
Original: {measurements.group(1)}m, {measurements.group(2)}kg

{VAR height_feet = measurements.group(1) * meters_to_feet}
{VAR weight_pounds = measurements.group(2) * kg_to_pounds}

Converted: {MATH height_feet} feet, {MATH weight_pounds} pounds
{/for}
```

## Summary

The MATH and VAR features extend the regex editor's capabilities to handle complex calculations and data processing workflows. They work seamlessly with existing pattern matching and conditional logic to create powerful text processing templates.

Key benefits:
- **Standalone calculations** with `{MATH}`
- **Variable storage** with `{VAR}`
- **Pattern integration** for data processing
- **Global scope** for complex workflows
- **Mathematical functions** for advanced calculations