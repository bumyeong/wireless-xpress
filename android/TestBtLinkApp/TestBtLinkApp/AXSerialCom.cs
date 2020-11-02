using System;
using System.IO.Ports;
using System.Threading;
using Diagnostics = System.Diagnostics;
using System.Collections.Concurrent;

namespace com.bumyeong.uart
{
    //-------------------------------------------------------------------
    //This AX-Fast Serial Library
    //Developer: Ahmed Mubarak - RoofMan
    //This Library Provide The Fastest & Efficient Serial Communication
    //Over The Standard C# Serial Component
    //-------------------------------------------------------------------

    public class SerialClient : IDisposable
    {
        private string _port = "COM1";
        private int _baudRate = 115200;
        private string _errorMessage = string.Empty;
        private bool _isContinue = false;

        private SerialPort _serialPort;
        private Thread _serialThread;
        private ConcurrentQueue<byte[]> _queue;

        /*The Critical Frequency of Communication to Avoid Any Lag*/
        //private const int freqCriticalLimit = 20;

        public SerialClient(ConcurrentQueue<byte[]> queue)
        {
            _queue = queue;

            _serialThread = new Thread(new ThreadStart(SerialReceiving))
            {
                Priority = ThreadPriority.Normal
            };
            _serialThread.Name = "SerialHandle" + _serialThread.ManagedThreadId.ToString();
        }

        public string Port
        {
            get { return _port; }
        }
        public int BaudRate
        {
            get { return _baudRate; }
        }
        public string ConnectionString
        {
            get
            {
                return String.Format("[Serial] Port: {0} | Baudrate: {1}", _serialPort.PortName, _serialPort.BaudRate.ToString());
            }
        }
        public string ErrorMessage
        {
            get { return _errorMessage; }
        }

        public bool OpenConn(string port = "", int baudRate = 115200)
        {
            if( port.Length > 0 )
                _port = port;

            _baudRate = baudRate;

            try
            {
                if (_serialPort == null)
                {
                    _serialPort = new SerialPort(_port, _baudRate, Parity.None, 8, StopBits.One);
                    _serialPort.ReadBufferSize = 921600;
                }

                if (!_serialPort.IsOpen)
                {
                    _serialPort.ReadTimeout = -1;
                    _serialPort.WriteTimeout = -1;

                    _serialPort.Open();

                    if (_serialPort.IsOpen)
                    {
                        _serialThread = new Thread(new ThreadStart(SerialReceiving))
                        {
                            Priority = ThreadPriority.Normal
                        };
                        _serialThread.Name = "SerialHandle" + _serialThread.ManagedThreadId.ToString();

                        _serialThread.Start(); /*Start The Communication Thread*/
                        _isContinue = true;
                    }
                }
            }
            catch (Exception ex)
            {
                _errorMessage = ex.Message;
                return false;
            }

            return true;
        }

        public void CloseConn()
        {
            if (_serialPort != null && _serialPort.IsOpen)
            {
                _isContinue = false;
                Thread.Sleep(10);

                try
                {
                    if (_serialThread.Join(100) == false)
                    {
                        _serialThread.Abort();
                    }
                }
                catch (Exception ex)
                {
                    if (_serialThread.ThreadState != ThreadState.Aborted && _serialThread.ThreadState != ThreadState.Stopped)
                    {
                        _errorMessage = ex.Message;
                        _serialThread.Abort();
                    }
                }

                _serialPort.Close();
            }
        }

        public bool ResetConn()
        {
            CloseConn();
            return OpenConn();
        }

        public void Dispose()
        {
            CloseConn();

            if (_serialPort != null)
            {
                _serialPort.Dispose();
                _serialPort = null;
            }
        }

        public bool WriteByteArray(byte[] data)
        {
            try
            {
                _serialPort.Write(data, 0, data.Length);
            }
            catch(Exception ex)
            {
                _errorMessage = ex.Message;
                return false;
            }

            return true;
        }

        public bool WriteString(string data)
        {
            try
            {
                _serialPort.WriteLine(data);
            }
            catch (Exception ex)
            {
                _errorMessage = ex.Message;
                return false;
            }

            return true;
        }

        //---------------------------------//
        //            READ THREAD 
        //---------------------------------//
        private void SerialReceiving()
        {
            while (_isContinue)
            {
                int count = _serialPort.BytesToRead;

                if (count < 1)
                {
                    Thread.Sleep(5);
                    continue;
                }

                /*Form The Packet in The Buffer*/
                byte[] buf = new byte[count];
                _serialPort.Read(buf, 0, count);

                _queue.Enqueue(buf);
            }
        }
    }
}
