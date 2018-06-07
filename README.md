# Spaceship-Planet-X
A game that simulates space travel from Earth to a target planet on randomly generated maps with no known optimal path.
Implemented using DFW walk and Dijkstra's Shortest Path algorithm in Java. Classes that I implemented are in the folder "student".

Screenshot of the gameplay w/ a randomly generated map:
<img width="1322" alt="screen shot 2018-06-07 at 1 53 56 pm" src="https://user-images.githubusercontent.com/7096526/41117211-5d850dc0-6a5a-11e8-8131-3f1839d3e24c.png">


Details:
A spaceship is initialized on Earth at the start of the game. Another spaceship is randomly placed on another planet – Planet X – 
where the crew of that spaceship is stranded and requires the user to rescue them. The user's spaceship has to optimally traverse
the random graph to find the stranded crew without running out of fuel (a constraint in the game). Once the user finds Planet X,
she or he must return to Earth with two conditions: 1) without running out of fuel and 2)collecting minerals on every planet visited along the way. 

This project was the final project for CS 2110 (OOP & Data Structures) at Cornell. Skeleton code was provided by CS 2110 instructors
and the course staff.
