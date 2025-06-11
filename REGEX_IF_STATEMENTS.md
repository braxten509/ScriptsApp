# Regex Editor - If Statements and Mathematical Comparisons

## Overview

The regex editor now supports conditional logic with mathematical comparisons and functions. This allows for sophisticated template processing based on pattern matches.

## Fixed Issue

**Problem**: If statements within for loops were not evaluating properly. All conditions were returning the same result regardless of the actual match values.

**Example of broken behavior**:
```
Input: Price $50, Price $120, Price $8
Pattern 'prices': \$(\d+)

Template:
{for prices}
Price: ${prices.group(1)}
{if prices.group(1) > 100} - Premium item{/if}
{if prices.group(1) < 20} - Budget item{/if}
{/for}

Broken Output:
Price: $50
 - Premium item
Price: $120
 - Premium item 
Price: $8
 - Premium item
```

**Solution**: Modified the template processing to pass loop context (current pattern and index) to condition evaluation, ensuring each loop iteration evaluates conditions against the correct match values.

## Fixed Implementation

The fix involved:

1. **Enhanced `processTemplateScript` method**: Now passes current loop context to nested template processing
2. **Updated `evaluateCondition` method**: Handles pattern references within loop context correctly
3. **Modified `processVariable` method**: Properly resolves pattern group references for current loop iteration

**Correct Output (Fixed)**:
```
Price: $50

Price: $120
 - Premium item

Price: $8

 - Budget item
```

## Syntax Reference

### Basic If Statements
```
{if condition}Content when true{/if}
```

### Mathematical Comparisons
- `<`, `>`, `<=`, `>=`, `==`, `!=`
- Example: `{if price > 100}Expensive{/if}`

### Mathematical Operations
- `+`, `-`, `*`, `/`, `%`
- Example: `{if price * 2 > 200}Double price exceeds 200{/if}`

### Mathematical Functions
- `abs(x)` - absolute value
- `sqrt(x)` - square root
- `pow(x,y)` - x to the power of y
- `min(x,y)` - minimum of two values
- `max(x,y)` - maximum of two values

### Pattern References in Conditions
- `pattern` - uses first match
- `pattern[index]` - uses specific match by index
- `pattern.group(n)` - uses capture group from first match
- `pattern[index].group(n)` - uses capture group from specific match
- Within loops: `pattern.group(n)` refers to current iteration's match

## Example Templates

### Price Classification
```
{for prices}
Price: ${prices.group(1)}
{if prices.group(1) < 20} - Budget item{/if}
{if prices.group(1) >= 20 && prices.group(1) <= 100} - Standard item{/if}
{if prices.group(1) > 100} - Premium item{/if}
{/for}
```

### Complex Calculations
```
{for temperatures}
Temperature: {temperatures.group(1)}°F
{if abs(temperatures.group(1) - 72) < 5} - Comfortable{/if}
{if temperatures.group(1) > 90} - Hot{/if}
{if temperatures.group(1) < 32} - Freezing{/if}
{/for}
```

### Mathematical Functions
```
{if sqrt(pow(x.group(1), 2) + pow(y.group(1), 2)) > 10}
Distance greater than 10
{/if}
```

## Implementation Details

### Key Methods Modified

1. **`processTemplateScript(String, Map<String, List<MatchResult>>, String, int)`**
   - Passes current pattern and index to nested processing
   - Ensures loop context is maintained through recursion

2. **`evaluateCondition(String, Map<String, List<MatchResult>>, String, int)`**
   - Handles pattern.group(n) references within loop context
   - Replaces pattern references with actual values before mathematical evaluation

3. **`processVariable(String, Map<String, List<MatchResult>>, String, int)`**
   - Resolves pattern group references for current loop iteration
   - Maintains backward compatibility with non-loop contexts

### Pattern Processing Priority
1. Current loop pattern references (e.g., `prices.group(1)` within `{for prices}`)
2. Indexed pattern references (e.g., `pattern[0].group(1)`)
3. General pattern references (e.g., `pattern.group(1)`)

This ensures that within loops, pattern references correctly refer to the current iteration's match rather than always using the first match.