
namespace TestBtLinkApp
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.btnSerialOpen = new System.Windows.Forms.Button();
            this.btnSendRequest = new System.Windows.Forms.Button();
            this.lstResultMessage = new System.Windows.Forms.ListBox();
            this.timerResponse = new System.Windows.Forms.Timer(this.components);
            this.SuspendLayout();
            // 
            // btnSerialOpen
            // 
            this.btnSerialOpen.Location = new System.Drawing.Point(15, 16);
            this.btnSerialOpen.Margin = new System.Windows.Forms.Padding(4);
            this.btnSerialOpen.Name = "btnSerialOpen";
            this.btnSerialOpen.Size = new System.Drawing.Size(252, 52);
            this.btnSerialOpen.TabIndex = 1;
            this.btnSerialOpen.Text = "OPEN Serial Port";
            this.btnSerialOpen.UseVisualStyleBackColor = true;
            this.btnSerialOpen.Click += new System.EventHandler(this.btnSerialOpen_Click);
            // 
            // btnSendRequest
            // 
            this.btnSendRequest.Location = new System.Drawing.Point(297, 16);
            this.btnSendRequest.Name = "btnSendRequest";
            this.btnSendRequest.Size = new System.Drawing.Size(252, 52);
            this.btnSendRequest.TabIndex = 2;
            this.btnSendRequest.Text = "SEND REQ.";
            this.btnSendRequest.UseVisualStyleBackColor = true;
            this.btnSendRequest.Click += new System.EventHandler(this.btnSendRequest_Click);
            // 
            // lstResultMessage
            // 
            this.lstResultMessage.Font = new System.Drawing.Font("GulimChe", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(129)));
            this.lstResultMessage.FormattingEnabled = true;
            this.lstResultMessage.HorizontalExtent = 1920;
            this.lstResultMessage.HorizontalScrollbar = true;
            this.lstResultMessage.Location = new System.Drawing.Point(15, 88);
            this.lstResultMessage.Name = "lstResultMessage";
            this.lstResultMessage.Size = new System.Drawing.Size(534, 407);
            this.lstResultMessage.TabIndex = 3;
            // 
            // timerResponse
            // 
            this.timerResponse.Interval = 1000;
            this.timerResponse.Tick += new System.EventHandler(this.timerResponse_Tick);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(567, 516);
            this.Controls.Add(this.lstResultMessage);
            this.Controls.Add(this.btnSendRequest);
            this.Controls.Add(this.btnSerialOpen);
            this.Font = new System.Drawing.Font("Gulim", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(129)));
            this.Margin = new System.Windows.Forms.Padding(4);
            this.Name = "Form1";
            this.Text = "Form1";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
            this.Load += new System.EventHandler(this.Form1_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button btnSerialOpen;
        private System.Windows.Forms.Button btnSendRequest;
        private System.Windows.Forms.ListBox lstResultMessage;
        private System.Windows.Forms.Timer timerResponse;
    }
}

