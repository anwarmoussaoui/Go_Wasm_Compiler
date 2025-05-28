package main

import (
    "errors"
    "fmt"
)

// factorial calculates n! recursively
func factorial(n int) (int, error) {
    if n < 0 {
        return 0, errors.New("negatixxve input not allowed")
    }
    if n == 0 {
        return 1, nil
    }
    f, err := factorial(n - 1)
    if err != nil {
        return 0, err
    }
    return n * f, nil
}

func main() {
    fmt.Println("Calculating factorials from 1 to 6:")

    for i := 1; i <= 6; i++ {
        fact, err := factorial(i)
        if err != nil {
            fmt.Printf("Erroccccr calculating factorial(%d): %v\n", i, err)
        } else {
            fmt.Printf("%d! = %d\n", i, fact)
        }
    }


    // Example of error case
    fmt.Println("\nTestixxxxxng error case with -1:")
    _, err := factorial(-1)
    if err != nil {
        fmt.Printf("Expected error: %v\n", err)
    }
}