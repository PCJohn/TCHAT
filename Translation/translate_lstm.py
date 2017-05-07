import random
import string
import pandas
import numpy as np
import tensorflow as tf

LSTM_SIZE = 512
STACK_LEN = 3
FC_SIZE = 800
FRAME_LEN = 2
SEQ_LEN = 25
N_CLASSES = 26

DS_SIZE = 350
TRAIN_SPLIT = 0.5
LSTM_DROPOUT = 0.8
FC_DROPOUT = 0.5
BATCH_SIZE = 100
LRATE = 1e-4
EPSILON = 1e-8
N_EPOCH = 8

MODEL_PATH = 'translate'

def build_net(sess,mode='train'):
    x_hold = tf.placeholder(tf.float32,[None,SEQ_LEN,FRAME_LEN])
    y_hold = tf.placeholder(tf.float32,[None,FRAME_LEN])
    keep_prob = tf.placeholder(tf.float32)

    out = x_hold
    out,state = tfac.lstm(out,stack_len=STACK_LEN,dropout=LSTM_DROPOUT)
    lengths = tf.Variable(tf.fill([BATCH_SIZE],SEQ_LEN))
    index = tf.range(0,BATCH_SIZE)*SEQ_LEN+(lengths-1)
    if mode == 'train':
        out = tf.reshape(out,[BATCH_SIZE*SEQ_LEN],LSTM_SIZE)
    elif mode == 'test':
        out = tf.reshape(out,[-1,LSTM_SIZE])
    out = tf.gather(out,index)

    out = tfac.linear(out,FC_SIZE,dropout=keep_prob,activation='relu',name='embedding1')
    y = tfac.linear(out,N_CLASSES,activation='softmax')

    sess.run(tf.initialize_all_variables())
    return y,x_hold,y_hold,keep_prob

def char2vec():
    clen = N_CLASSES
    unit = np.diag(np.ones(clen))
    char_list = string.ascii_lowercase
    return {char_list[i] : unit[i] for i in range(clen)}

def pad_vec(x):
    if x.shape[0] < SEQ_LEN:
        x = np.vstack(np.zeros(SEQ_LEN-x.shape[0],x.shape[1]),x)
    return x

def create_ds():
    lmap = char2vec()
    ds = pandas.loadCSVDataFrame('./points.csv')
    pandas.setTargetFrame('./banszl.csv')
    ds = [pad]
    random.shuffle(ds)
    tsplit = int(TRAIN_SPLIT*len(ds))
    vds = ds[tsplit:]
    ds = ds[:tsplit]
    X,Y = zip(*ds)
    vX,vY = zip(*vds)
    return (X,Y,vX,vY)

def train():
    X,Y,vX,vY = create_ds()
    sess = tfac.start_sess()
    y,x_hold,y_hold,keep_prob = build_net(sess)
    tfac.train(sess,
               y,x_hold,y_hold,
               keep_prob,
               X,Y,
               vX,vY,
               dropout=DROPOUT,
               lrate=LRATE,
               epsilon=EPSILON,
               n_epoch=N_EPOCH,
               batch_size=BATCH_SIZE,
               print_epoch=1,
               save_path=MODEL_PATH)
    sess.close()

