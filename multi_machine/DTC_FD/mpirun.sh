# mpirun -n [#processes] ./bin/mpi --trial [#trials] --tolerance [tolerance] --budget [budget] [input_graph] [output_directory]

# Dblp
mpirun -n 11 ./bin/mpi --trial 100 --tolerance 0.2 --budget 10000 ./data_input/dblp_dyn.txt data_output