#terminal-1
sbt "run-main remote.broker.Service localhost 2701"

#terminal-2
sbt "run-main remote.edge.Service 2701"
