import serial

def save_data(path_to_save, file_name, serial_port_name, timestamp):
	ser = serial.Serial(serial_port_name)
	ser.write(timestamp.encode())
	with open(path_to_save + '/' + file_name, 'a') as a_f:
		while True:
			line = ser.readline()
			a_f.write(line.decode())

def show_in_terminal(serial_port_name, timestamp):
	ser = serial.Serial(serial_port_name)
	ser.write(timestamp.encode())
	while True:
		line = ser.readline()
		#print(float(line.split()[-4].decode()))
		print(line)
serial_port_name = '/dev/ttyACM1'
timestamp = '10/10/1997 3:4:5'

file_name = 'test_data.txt'
path_to_save = '/home/bohdan/electron/data_saver/data/'





if __name__ == '__main__':
	show_in_terminal(serial_port_name, timestamp)
