import pandas as pd
import matplotlib.pyplot as plt
import glob
import re

def plot_histogram_from_csv(filename, inputs, dims, bins):
    print(f'Plotting histograms to {filename}...')
    fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(5, 5))
    axes = axes.flatten()

    for i, title in enumerate(inputs.keys()):
        file_path = inputs[title]
        # Reading data from CSV file
        data = pd.read_csv(file_path, header=None)
        # Ignore the first 100 entries; warming up the JVM
        data = data.iloc[100:]
        # Convert each datum to integer
        data = data.astype(int)
        # Remove outliers
        data = data[data[0] < dims['right']]

        # Uncomment to print the min, max, and average values - along with the file_path
        print(f'{title} - min: {data[0].min()}, max: {data[0].max()}, avg: {data[0].mean()}')
        
        # Plotting histogram
        axes[i].hist(data[0], bins=bins, color='blue', alpha=0.7)
        axes[i].set_title(title)
        axes[i].set_xlabel('Latency (ms)')
        axes[i].set_ylabel('Frequency')
        axes[i].set_xlim(left=dims['left'], right=dims['right'])
        axes[i].set_ylim(bottom=dims['bottom'], top=dims['top'])

    plt.tight_layout()
    plt.savefig(filename, format='png')
    print(f'Done.')

def plot_producers_histograms():
    inputs = {
        'Worker 1 (Ozone)': 'data/concurrentproducers/worker1-latencies.csv', 
        'Worker 2 (Ozone)': 'data/concurrentproducers/worker2-latencies.csv', 
        'Worker 1 (Choral)': 'data/inorderproducers/worker1-latencies.csv', 
        'Worker 2 (Choral)': 'data/inorderproducers/worker2-latencies.csv'
    }
    dims = {
        'left': 0,
        'right': 20,
        'bottom': 0,
        'top': 400
    }

    plot_histogram_from_csv("figures/producers.png", inputs, dims, bins=10)

def plot_senders_histograms():
    inputs = {
        'txt (Ozone)': 'data/concurrentsend/txt-latencies.csv', 
        'key (Ozone)': 'data/concurrentsend/key-latencies.csv', 
        'txt (Choral)': 'data/inordersend/txt-latencies.csv', 
        'key (Choral)': 'data/inordersend/key-latencies.csv'
    }
    dims = {
        'left': 0,
        'right': 18,
        'bottom': 0,
        'top': 800
    }

    plot_histogram_from_csv("figures/senders.png", inputs, dims, bins=15)


####################################################################################################
# Model serving
####################################################################################################

def find_modelserving_rates(batch_size):
    ozone_files = glob.glob('data/modelserving/throughput-concurrent-rate*-batch*.csv')
    choral_files = glob.glob('data/modelserving/throughput-inorder-rate*-batch*.csv')

    ozone_rates = sorted([int(re.search('rate(\d+)-batch', file).group(1)) for file in ozone_files])
    choral_rates = sorted([int(re.search('rate(\d+)-batch', file).group(1)) for file in choral_files])

    return ozone_rates, choral_rates

def plot_modelserving_throughput(batch_size):
    ozone_rates, choral_rates = find_modelserving_rates(batch_size)

    ozone_throughputs = []
    choral_throughputs = []

    for rate in ozone_rates:
        path = f'data/modelserving/throughput-concurrent-rate{rate}-batch{batch_size}.csv'
        with open(path, 'r') as f:
            throughput = int(f.read())
            ozone_throughputs.append(throughput)

    for rate in choral_rates:
        path = f'data/modelserving/throughput-inorder-rate{rate}-batch{batch_size}.csv'
        with open(path, 'r') as f:
            throughput = int(f.read())
            choral_throughputs.append(throughput)

    plt.plot(ozone_rates, ozone_throughputs, 'o-', label='Ozone')
    plt.plot(choral_rates, choral_throughputs, 'x-', label='Choral')
    plt.xlabel('Requests per second')
    plt.ylabel('Throughput (responses per second)')
    plt.legend()
    plt.show()

def calculate_latency_percentile(data, percentile):
    data = data.iloc[100:]
    data = data.astype(int)
    return data[0].quantile(percentile / 100)

def plot_modelserving_99pi(batch_size):
    ozone_rates, choral_rates = find_modelserving_rates(batch_size)

    ozone_latency = []
    choral_latency = []

    for rate in ozone_rates:
        path = f'data/modelserving/latency-concurrent-rate{rate}-batch{batch_size}.csv'
        data = pd.read_csv(path, header=None)
        latency = calculate_latency_percentile(data, 99)
        ozone_latency.append(latency)

    for rate in choral_rates:
        path = f'data/modelserving/latency-inorder-rate{rate}-batch{batch_size}.csv'
        data = pd.read_csv(path, header=None)
        latency = calculate_latency_percentile(data, 99)
        choral_latency.append(latency)

    plt.plot(ozone_rates, ozone_latency, 'o-', label='Ozone')
    plt.plot(choral_rates, choral_latency, 'x-', label='Choral')
    plt.xlabel('Requests per second')
    plt.ylabel('99th percentile latency (ms)')
    plt.legend()
    plt.show()

def plot_modelserving_histograms(batch_size):
    inputs = {
        'Ozone': f'data/modelserving/latency-concurrent-rate175-batch{batch_size}.csv', 
        'Choral': f'data/modelserving/latency-inorder-rate175-batch{batch_size}.csv'
    }
    dims = {
        'left': 0,
        'right': 1100,
        'bottom': 0,
        'top': 40
    }

    plot_histogram_from_csv("figures/modelserving.png", inputs, dims, bins=50)


#plot_producers_histograms()
#plot_senders_histograms()
plot_modelserving_throughput(10)
plot_modelserving_99pi(10)
#plot_modelserving_histograms(10)