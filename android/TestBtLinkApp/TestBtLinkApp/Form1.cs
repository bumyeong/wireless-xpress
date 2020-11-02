using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

using com.bumyeong.uart;

namespace TestBtLinkApp
{
    public partial class Form1 : Form
    {
        private bool isSerialOpen = false;
        private string serialPortName = "COM1";

        protected SerialClient serialClient;

        // RECEIVE THREAD PART
        protected bool isProcessContinue = false;
        protected ConcurrentQueue<byte[]> receiveQueue;
        protected Thread processPortDataThread;

        private const string REQUEST_STREAM =  "10 02 02 01 01 10 03 04 ";
        private const string RESPONSE_STREAM = "10 02 02 03 02 10 03 07 ";

        private byte[] REQUEST_BYTE_STREAM = { 0x10, 0x02, 0x02, 0x01, 0x02, 0x10, 0x03, 0x05 };
        private byte[] RESPONSE_BYTE_STREAM = { 0x10, 0x02, 0x02, 0x03, 0x02, 0x10, 0x03, 0x07 };
        private byte[] ACK_BYTE_STREAM = { 0x10, 0x02, 0x01, 0x05, 0x10, 0x03, 0x07 };

        public Form1()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            this.receiveQueue = new ConcurrentQueue<byte[]>();
            this.serialClient = new SerialClient(this.receiveQueue);
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (this.isSerialOpen == true)
            {
                btnSerialOpen_Click(null, null);
            }

            if (this.serialClient != null)
            {
                this.serialClient.Dispose();
            }
        }

        private void AddResultMessage(string message)
        {
            this.lstResultMessage.Items.Add(message);
            this.lstResultMessage.SetSelected(this.lstResultMessage.Items.Count - 1, true);

            if(message.Contains(REQUEST_STREAM) == true)
            {
                this.serialClient.WriteByteArray(ACK_BYTE_STREAM);
                this.timerResponse.Start();
            }
            else if( message.Contains(RESPONSE_STREAM) == true)
            {
                this.serialClient.WriteByteArray(ACK_BYTE_STREAM);
            }
        }

        private void AddResultMessage(byte[] stream)
        {
            string message = ">>> ";

            foreach( byte data in stream)
            {
                message += string.Format("{0:X2} ", data);
            }

            this.lstResultMessage.Items.Add(message);
            this.lstResultMessage.SetSelected(this.lstResultMessage.Items.Count - 1, true);
        }

        private void ErrorResultMessage(string message)
        {
            this.lstResultMessage.Items.Add(">>>ERR - " + message);
            this.lstResultMessage.SetSelected(this.lstResultMessage.Items.Count - 1, true);
        }

        private void btnSerialOpen_Click(object sender, EventArgs e)
        {
            if (isSerialOpen == true)
            {
                this.serialClient.CloseConn();

                this.isProcessContinue = false;
                Thread.Sleep(10);

                try
                {
                    if (this.processPortDataThread.Join(100) == false)
                    {
                        this.processPortDataThread.Abort();
                    }
                }
                catch (Exception ex)
                {
                    if (this.processPortDataThread.ThreadState != System.Threading.ThreadState.Aborted && this.processPortDataThread.ThreadState != System.Threading.ThreadState.Stopped)
                    {
                        this.processPortDataThread.Abort();
                        ErrorResultMessage(ex.Message);
                    }
                }

                byte[] data;
                while (this.receiveQueue.TryDequeue(out data))
                {
                    // CLEAR QUEUE
                }

                this.isSerialOpen = false;
                this.btnSerialOpen.Text = "OPEN Serial Port";
            }
            else
            {
                this.lstResultMessage.Items.Clear();

                frmSelectSerialPort dlgPort = new frmSelectSerialPort();
                dlgPort.selectName = ConfigurationManager.AppSettings["SelectComPort"];

                if (dlgPort.ShowDialog() != DialogResult.OK)
                {
                    return;
                }

                this.serialPortName = dlgPort.selectName;

                if (this.serialClient.OpenConn(this.serialPortName) == false)
                {
                    ErrorResultMessage(this.serialClient.ErrorMessage);
                    return;
                }

                try
                {
                    this.processPortDataThread = new Thread(new ThreadStart(this.ProcessPortDataInQueue))
                    {
                        Priority = ThreadPriority.Normal
                    };

                    this.processPortDataThread.Name = "ProcessQueue" + this.processPortDataThread.ManagedThreadId.ToString();

                    this.isProcessContinue = true;
                    this.processPortDataThread.Start();
                }
                catch (Exception ex)
                {
                    ErrorResultMessage(ex.Message);
                    return;
                }

                // OPEN SERIAL PORT
                this.serialPortName = dlgPort.selectName;

                this.isSerialOpen = true;
                this.btnSerialOpen.Text = "CLOSE Serial Port";

                Configuration config = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
                config.AppSettings.Settings["SelectComPort"].Value = serialPortName;
                config.Save(ConfigurationSaveMode.Modified);
                ConfigurationManager.RefreshSection("appSettings");
            }
        }

        private void ProcessPortDataInQueue()
        {
            byte[] portData;
            StringBuilder byteStream = new StringBuilder(256);

            while (this.isProcessContinue)
            {
                if (this.receiveQueue.TryDequeue(out portData) == false)
                {
                    Thread.Sleep(10);
                    continue;
                }

                byteStream.Clear();

                foreach (byte inputData in portData)
                {
                    byteStream.Append(string.Format("{0:X2} ", inputData));
                }

                this.Invoke(new Action(delegate ()
                {
                    AddResultMessage("<<< " + byteStream.ToString());
                }));
            }
        }

        private void btnSendRequest_Click(object sender, EventArgs e)
        {
            AddResultMessage(REQUEST_BYTE_STREAM);
            this.serialClient.WriteByteArray(REQUEST_BYTE_STREAM);
        }

        private void timerResponse_Tick(object sender, EventArgs e)
        {
            this.timerResponse.Stop();

            AddResultMessage(RESPONSE_BYTE_STREAM);
            this.serialClient.WriteByteArray(RESPONSE_BYTE_STREAM);
        }
    }
}
