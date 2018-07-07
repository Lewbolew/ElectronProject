import dash
from dash.dependencies import Output, Event
import dash_core_components as dcc
import dash_html_components as html
import plotly
import random
import numpy as np
import pandas as pd
import plotly.graph_objs as go
import serial 
from scipy.fftpack import fft, ifft, fftshift,fftfreq, ifftn

def transform_data(x):
    ft = fft(x)


# Here you should put the comport of the accelerometer
serial_port_name = '/dev/ttyACM1'
# here you should put random data in this format to start the data transmition 
timestamp = '10/10/1997 3:4:5'
ser = serial.Serial(serial_port_name)
ser.write(timestamp.encode())

app = dash.Dash(__name__)
app.layout = html.Div(

    [
        dcc.Graph(id='live-graph', animate=False,
            figure={
        'data': [{'y': [0 for x in range(400)], 'type': 'bar'}]}
        ),
        dcc.Interval(
            id='graph-update',
            interval=500
        ),
    ]

)

@app.callback(Output('live-graph', 'figure'),
              events=[Event('graph-update', 'interval')])
def update_graph_scatter():
    X = list()
    while len(X) < 200:
        line = ser.readline()
        X.append(float(line.split()[-4].decode()))
    X = fft(X)
    X = [np.linalg.norm(k) for k in X]
    data = {'y': X, 'type': 'bar'}
    return {'data': [data],
    'layout' : go.Layout(
        xaxis=dict(range=[0,201]),
        yaxis=dict(range=[0,50])
        )
    }



if __name__ == '__main__':
    app.run_server(debug=True)
