using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO.Ports;
using System.Linq;
using System.Management;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace TestBtLinkApp
{
    public partial class frmSelectSerialPort : Form
    {
        public string selectName = "COM1";

        public frmSelectSerialPort()
        {
            InitializeComponent();
        }

        private void frmSelectSerialPort_Load(object sender, EventArgs e)
        {
            string[] serialPortNames = SerialPort.GetPortNames();

            using (var searcher = new ManagementObjectSearcher("SELECT * FROM WIN32_SerialPort"))
            {
                var ports = searcher.Get().Cast<ManagementBaseObject>().ToList();
                var tList = (from n in serialPortNames
                             join p in ports on n equals p["DeviceID"].ToString()
                             select n + "-" + p["Caption"]).ToList();

                this.lstPortList.Items.AddRange(tList.ToArray());
            }

            bool isFound = false;
            for (int index = 0; index < this.lstPortList.Items.Count; index++)
            {
                if (this.lstPortList.Items[index].ToString().Contains(this.selectName))
                {
                    isFound = true;
                    this.lstPortList.SelectedIndex = index;
                }
            }

            if (isFound == false)
            {
                this.lstPortList.SelectedIndex = 0;
            }
        }

        private void btnOpen_Click(object sender, EventArgs e)
        {
            string[] splits = this.lstPortList.SelectedItem.ToString().Split('-');
            this.selectName = splits[0].Trim();
            Close();
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            Close();
        }
    }
}
