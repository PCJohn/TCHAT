"""
Accessory methods for using TensorFlow: Includes support methods for convnet functions

Author: Prithvijit Chakrabarty (prithvijit.chakrabarty@intel.com)
"""
import shutil
import os
import numpy as np
import random
import tensorflow as tf
from tensorflow.contrib.tensorboard.plugins import projector

#Make weight and bias variables -- From the TensorFlow tutorial
def weight(shape,name=None):
    if name is None:
        intial = tf.truncated_normal(shape, stddev=0.1)
    else:
        intial = tf.truncated_normal(shape, stddev=0.1, name=name)
    return tf.Variable(intial)

def bias(shape,name=None):
    if name is None:
        intial = tf.constant(0.1, shape=shape)
    else:
        intial = tf.constant(0.1, shape=shape, name=name)
    return tf.Variable(intial)

#Finds the product of a dimension tuple to find the total length
def dim_prod(dim_arr):
    return np.prod([d for d in dim_arr if d != None])

#3D covolution layer
def conv3d(in_v,ker=[1,1,1],strides=[1,1,1],pad='SAME',out_ch=1,use_bias=True):
    in_ch = in_v.get_shape().as_list()[-1]
    w = weight(ker+[in_ch,out_ch])
    out = tf.nn.conv3d(in_v,w,strides=[1]+strides+[1],padding=pad)
    if use_bias == True:
        b = bias([out_ch])
        out = out+b
    out = tf.nn.relu(out)
    dim = in_v.get_shape().as_list()[1:]
    for i in range(3):
       if pad == 'VALID':
           dim[i] = (dim[i]-ker[i])
           dim[i] = dim[i]/strides[i]+1
    dim[-1] = out_ch
    return np.prod(dim),out

#2D covolution layer
def conv2d(in_v,ker=[1,1],strides=[1,1],pad='SAME',out_ch=1):
    in_ch = in_v.get_shape().as_list()[-1]
    w = weight(ker+[in_ch,out_ch])
    b = bias([out_ch])
    out = tf.nn.relu(tf.nn.conv2d(in_v,w,strides=[1]+strides+[1],padding=pad)+b)
    dim = in_v.get_shape().as_list()[1:]
    for i in range(2):
       if pad == 'VALID':
           dim[i] = (dim[i]-ker[i])
           dim[i] = dim[i]/strides[i]+1
    dim[-1] = out_ch
    return np.prod(dim),out

def lstm(in_v,lstm_size,stack_len=-1,dropout=-1):
    cell = tf.nn.rnn_cell.LSTMCell(lstm_size,state_is_tuple=True)
    if dropout != -1:
        cell = tf.nn.rnn_cell.DropoutWrapper(cell,output_keep_prob=dropout)
    if stack_len != -1:
        cell = tf.nn.rnn_cell.MultiRNNCell([cell]*stack_len,state_is_tuple=True)
    out,state = tf.nn.dynamic(cell, in_v, dtype=tf.float32)
    return out,state

#Fully connected layer
def fc(in_v,fc_size,dropout=-1,activation='relu',name=None):
    in_dim = in_v.get_shape().as_list()[-1]
    in_dim = np.int64(in_dim)
    fc_size = np.int64(fc_size)
    if name is None:
        w = weight([in_dim,fc_size])
        b = bias([fc_size])
    else:
        w = weight([in_dim,fc_size],name=name+'_wt')
        b = bias([fc_size],name=name+'_bs')
    out = tf.matmul(in_v,w)+b
    if activation == 'relu':
        out = tf.nn.relu(out)
    elif activation == 'softmax':
        out = tf.nn.softmax(out)
    elif activation == 'sigmoid':
        out = tf.nn.sigmoid(out)
    if dropout != -1:
        out = tf.nn.dropout(out,dropout)
    return out

#Start a TensorFlow session
def start_sess():
    config = tf.ConfigProto()
    config.gpu_options.allocator_type = 'BFC'
    sess = tf.Session(config=config)
    return sess

#Train the model
def train(sess,
          y, x_hold, y_hold,
          keep_prob,
          X, Y,
          valX, valY,
          dropout=0.5,
          lrate=1e-4,
          epsilon=1e-8,
          n_epoch=10,
          batch_size=50,
          print_epoch=1,
          save_path=None,
          continue_learning=False):
    print(x_hold.get_shape(),'--',y_hold.get_shape(),'--',X.shape,'--',Y.shape)
    cross_entropy = tf.reduce_mean(-tf.reduce_sum(y_hold*tf.log(y+1e-10),reduction_indices=[1]))
    train_step = tf.train.AdamOptimizer(learning_rate=lrate,epsilon=epsilon).minimize(cross_entropy)
    correct_prediction = tf.equal(tf.argmax(y,1),tf.argmax(y_hold,1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
    if not save_path is None:
        tf.scalar_summary('objective function',cross_entropy)
        merged_summ = tf.merge_all_summaries()
        saver = tf.train.Saver(tf.all_variables())
        if os.path.exists(save_path+'_graph'):
            shutil.rmtree(save_path+'_graph')
        os.makedirs(save_path+'_graph')
        writer = tf.train.SummaryWriter(save_path+'_graph',sess.graph)

    print('Starting training session...')
    sess.run(tf.initialize_all_variables())
    #sess.run(tf.global_variables_initializer())
    if continue_learning == True:
        saver = tf.train.Saver(tf.all_variables())
        saver.restore(sess,save_path)
    batch_num = 0
    batches = batchify(X,Y,batch_size)
    print('Number of batches:',len(batches))
    avg_acc = 0
    for i in range(n_epoch):
        avg_acc = 0
        random.shuffle(batches)
        for batchX,batchY in batches:
            batch_acc = accuracy.eval(session=sess, feed_dict={x_hold:batchX, y_hold:batchY, keep_prob:1})
            avg_acc = avg_acc + batch_acc
            sess.run(train_step,feed_dict={x_hold:batchX, y_hold:batchY, keep_prob:dropout})
        avg_acc = float(avg_acc)/len(batches)
        print('Epoch '+str(i)+': '+str(avg_acc))
        if not save_path is None:
            if i%print_epoch == 0:
                summary = sess.run(merged_summ,feed_dict={x_hold:batchX, y_hold:batchY, keep_prob:dropout})
                writer.add_summary(summary,i)
                saver.save(sess,os.path.join(save_path+'_graph','chkpnt'+str(i)+'.ckpt'),i)

    val_acc = -1
    if (not valX is None) & (not valY is None):
        #Validation
        val_acc = accuracy.eval(session=sess,feed_dict={x_hold:valX, y_hold:valY, keep_prob:1})
        print('Val acc:',val_acc)

    if not save_path is None:
        saver.save(sess,save_path)
        writer.flush()
        writer.close()
        print('Model saved')
    if val_acc != -1:
        return val_acc
    return avg_acc

def embeddings(X,Y):
    config = projector.ProjectorConfig()
    writer = tf.summary.FileWriter(save_path+'_graph',sess.graph)
    embedding_vars = [v for v in tf.global_variables() if v.name.startswith('embedding')]
    for v in enbedding_vars:
        sess.run(v.initializer)
    embedding = config.embeddings.add()
    projector.visualize_embeddings(writer,config)

#Test a model
def test(sess, X, Y, model_path):
    correct_prediction = tf.equal(tf.argmax(self.net,1), tf.argmax(self.y_hold,1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
    saver = tf.train.Saver()
    sess.run(tf.initialize_all_variables())
    saver.restore(sess,model_path)
    X = X.reshape((X.shape[0],X.shape[1]*X.shape[2]))
    test_accuracy = accuracy.eval(session=sess,feed_dict={x_hold:X,y_hold:Y,keep_prob:1})
    return test_accuracy

#Split to mini batches
def batchify(X, Y, batch_size):
    batches = [(X[i:i+batch_size],Y[i:i+batch_size]) for i in range(0,X.shape[0]-batch_size,batch_size)]
    random.shuffle(batches)
    return batches
