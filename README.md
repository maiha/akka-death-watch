#terminal-1
# start broker that is backend service
#   (listens on localhost:2701)
sbt "run-main remote.broker.Service localhost:2701"

#terminal-2
# start edge that is frontend service like Play
#   (listens on localhost:2801 with connecting to localhost:2701)
sbt "run-main remote.edge.Service localhost:2801 localhost:2701"

#terminal-3
# not working
(sbt "run-main client.Sender 2701")
