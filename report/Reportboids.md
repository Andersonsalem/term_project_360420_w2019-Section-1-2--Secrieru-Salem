Simulation of bird migration through logic
By Salem Andrew and Secrieru Dragos
Introduction: 

The mind of an animal is a big mystery to humans; unable to communicate with them it is impossible for us to know how it is working. 
Does it work by instinct ? Do animals use their own will to conduct their actions ? Is there any action that is not conducted for the pure sake of survival ?
Answering such question is a hard task and a possible way to do so would be to take an animal’s behavior and try to simulate it by also setting a number of rules. 
If the simulation of such behavior is successful when using a set of primitive and logical rule reflecting a purely survivalist mindset 
it is then possible to assume that this behavior is depending on instinct and not on a form of free will.

In this term project we will try to simulate bird flocks through a comparison between a set of boids created by us and many videos of bird flocking 
trying to confirm whether or not our set of rules is correct or not.

Rules and clarifications:
We will first start by giving some clarifications:
1-	If we are to study a system where food, sources of energy and predators are not present it is safe to assume that any form of random displacement is due to a “survivalistic drive” : if there is no food or predator to direct a flock’s directions then it will wander randomly in search of food.
2-	If there is a predator then randomness can only be called upon when out of its line of sight.
3-	We shall not consider any of the following factors:
a.	The age of the birds
b.	Their evolutionary fitness
c.	Different sight capacities
d.	The wind’s direction and speed
e.	An energy capacity (ie: our boids absolutely never get tired)
Our set of rules:
1-	Boids have a certain line of sight in which they detect other objects.
2-	Boids next to each other start aggregating BUT:
3-	Boids will never get too close to each other
4-	Boids will always try to flow in a common direction.
5-	Boids will get radially away from their predators.
6-	Predators will always try to catch the boid closest to them.
7-	Boids and predator will fly randomly until they get in each other’s line of sight.


