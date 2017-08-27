# AKKA-STREAM-PLAY-MONGO 
A batch is starting by generating X persons into a kafka topic called persons
akka-stream will pull from kafka(persons) save raw data to mongo(persons) and sort the people to adults and kids which are additional kafka topics
additional stream will offload from kids and adults into mongo and enrich kids with type of school: kindergarten, elementary or high

#API
POST /batch {"id":[unique batch name],"wanted":[number of persons in the batch]}

GET /batch list of recent batches

GET /batch/:id statistics on progress of batch

#RUN
to run the application (or tests)
docker-compose up
sbt run

live demo on: http://ec2-13-59-172-49.us-east-2.compute.amazonaws.com:9000/
