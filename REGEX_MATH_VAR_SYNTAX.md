# Regex Editor: MATH/VAR/SHOW Syntax Guide

## Important: Pattern References Inside MATH/VAR/SHOW

When referencing patterns inside MATH, VAR, or SHOW expressions, **DO NOT use curly braces** around the pattern reference.

### ❌ INCORRECT (will cause errors):
```
{VAR totalExpired += {ExpiredPoints.group(1)}}
{MATH counter = {PatternName.length()}}
{SHOW {MyPattern.group(2)}}
```

### ✅ CORRECT:
```
{VAR totalExpired += ExpiredPoints.group(1)}
{MATH counter = PatternName.length()}
{SHOW MyPattern.group(2)}
```

## Complete Example

Here's a working example that adds up expired points:

```
{VAR totalExpired = 0}
{for ExpiredPoints}
{ExpiredPoints.group(1)} points expired on {ExpiredPoints.group(2)}
{MATH totalExpired += ExpiredPoints.group(1)}
{/for}

Total Expired: {SHOW totalExpired}
```

## Command Reference

### MATH (Silent Calculation)
- **Purpose**: Performs calculations without displaying results
- **Syntax**: `{MATH expression}`
- **Examples**:
  - `{MATH counter = 0}` - Initialize variable
  - `{MATH total += price}` - Add to variable
  - `{MATH result = PatternName.group(1) * 2}` - Use pattern values

### SHOW (Display Result)
- **Purpose**: Evaluates expression and displays the result
- **Syntax**: `{SHOW expression}`
- **Examples**:
  - `{SHOW counter}` - Display variable value
  - `{SHOW 5 + 3}` - Display calculation result
  - `{SHOW PatternName.length()}` - Display pattern match count

### VAR (Variable Declaration)
- **Purpose**: Declare and assign variables
- **Syntax**: `{VAR name = expression}`
- **Examples**:
  - `{VAR total = 0}` - Initialize variable
  - `{VAR tax = price * 0.08}` - Calculate and store
  - `{VAR count += PatternName.length()}` - Accumulate values

## Pattern Reference Syntax

Inside MATH/VAR/SHOW expressions, use these formats:

- `PatternName` - The full match
- `PatternName.group(n)` - Group n of the match
- `PatternName.length()` - Number of matches
- `PatternName[index].group(n)` - Specific match by index

**Remember**: No curly braces around pattern references inside expressions!