# Debug - Actual vs Expected Output

## Your Test Case
**Input**: `Price $50, Price $120, Price $8`
**Pattern name**: `prices`  
**Pattern regex**: `\$(\d+)`
**Template**:
```
{for prices}
Price: ${prices.group(1)}
{if prices.group(1) > 100} - Premium item{/if}
{if prices.group(1) < 20} - Budget item{/if}
{/for}
```

## What You're Getting
```
Price: $50
Price: $120
Price: $8
```

## What Should Happen
```
Price: $50

Price: $120
 - Premium item

Price: $8

 - Budget item
```

## Debug Questions

1. Are the if statements being processed at all, or are they being ignored completely?
2. Are the conditions evaluating to false for all cases?
3. Is the pattern matching working correctly?

Let me create a simple debug version to test step by step.