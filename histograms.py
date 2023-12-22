import pandas as pd
import matplotlib.pyplot as plt

def plot_histogram_from_csv(inputs, dims, bins):
    fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(5, 5))
    axes = axes.flatten()

    for i, title in enumerate(inputs.keys()):
        file_path = inputs[title]
        # Reading data from CSV file
        data = pd.read_csv(file_path, header=None)
        # Ignore the first 100 entries; warming up the JVM
        data = data.iloc[100:]

        # Print the min, max, and average values - along with the file_path
        print(f'{title} - min: {data[0].min()}, max: {data[0].max()}, avg: {data[0].mean()}')
        
        # Plotting histogram
        axes[i].hist(data[0], bins=bins, color='blue', alpha=0.7)
        axes[i].set_title(title)
        axes[i].set_xlabel('Latency (ms)')
        axes[i].set_ylabel('Frequency')
        axes[i].set_xlim(left=dims['left'], right=dims['right'])
        axes[i].set_ylim(bottom=dims['bottom'], top=dims['top'])

    plt.tight_layout()
    plt.show()

def plot_producers_histograms():
    inputs = {
        'Worker 1 (Ozone)': 'data/concurrentproducers/worker1-latencies.csv', 
        'Worker 2 (Ozone)': 'data/concurrentproducers/worker2-latencies.csv', 
        'Worker 1 (Choral)': 'data/inorderproducers/worker1-latencies.csv', 
        'Worker 2 (Choral)': 'data/inorderproducers/worker2-latencies.csv'
    }
    dims = {
        'left': 0,
        'right': 200,
        'bottom': 0,
        'top': 80
    }

    plot_histogram_from_csv(inputs, dims, bins=30)

def plot_senders_histograms():
    inputs = {
        'txt (Ozone)': 'data/concurrentsend/key-latencies.csv', 
        'key (Ozone)': 'data/concurrentsend/txt-latencies.csv', 
        'txt (Choral)': 'data/inordersend/key-latencies.csv', 
        'key (Choral)': 'data/inordersend/txt-latencies.csv'
    }
    dims = {
        'left': 0,
        'right': 18,
        'bottom': 0,
        'top': 330
    }

    plot_histogram_from_csv(inputs, dims, bins=50)

plot_producers_histograms()
# plot_senders_histograms()
