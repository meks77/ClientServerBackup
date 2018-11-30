import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-navigationbar',
  templateUrl: './navigationbar.component.html',
  styleUrls: ['./navigationbar.component.css']
})
export class NavigationbarComponent implements OnInit {

  private mySidebar:HTMLElement;
  private overlayBg:HTMLElement;
  private logo = "./assets/logo-small.png";

  constructor() { }

  ngOnInit() {
    this.mySidebar = document.getElementById("mySidebar");
    this.overlayBg = document.getElementById("myOverlay");
  }

  openNavigaion() {
    if (this.mySidebar.style.display === 'block') {
      this.mySidebar.style.display = 'none';
      this.overlayBg.style.display = "none";
    } else {
      this.mySidebar.style.display = 'block';
      this.overlayBg.style.display = "block";
    }
  }

  hideNavigation() {
    this.mySidebar.style.display = "none";
    this.overlayBg.style.display = "none";
  }

  showOverView() {
    console.log("overview clicked");
    this.hideNavigation();
  }
}
