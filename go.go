package main

import (
    "fmt"
    "time"
)

func main() {
    for i := 1; i <= 5; i++ {
        fmt.Printf("Step %d\n", i)
        time.Sleep(500 * time.Millisecond)
    }
    fmt.Println("Loop finished!")
}
