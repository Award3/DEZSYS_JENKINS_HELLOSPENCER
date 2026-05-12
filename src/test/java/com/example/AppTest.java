package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void testGreet() {
        App app = new App();
        assertEquals("Hello, Nico!", app.greet("Nico"));
    }

    @Test
    void testGreetEmpty() {
        App app = new App();
        assertEquals("Hello, !", app.greet(""));
    }

    @Test
    void testAdd() {
        App app = new App();
        assertEquals(5, app.add(2, 3));
    }

    @Test
    void testAddNegative() {
        App app = new App();
        assertEquals(-1, app.add(2, -3));
    }
}